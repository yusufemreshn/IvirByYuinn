package com.yusufemre.ivirbyyuinn.ui.task

import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.Recurrence
import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.data.TaskTemplateEntity
import com.yusufemre.ivirbyyuinn.domain.ParsedDate
import com.yusufemre.ivirbyyuinn.notification.hasExactAlarmPermission
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDialog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Bu ekrandaki hemen hemen her etkileşimli öğe (öncelik/kategori/tekrar
// çipleri, tarih/saat ve şablon butonları, Kaydet/Vazgeç) kaydırılabilir
// bir Column'un İÇİNDE yaşıyor. Bunların hiçbiri kendi üzerine gelen bir
// SÜRÜKLEMEYİ tüketmiyor (clickable/FilterChip sadece dokunmayı/tıklamayı
// yakalıyor), o yüzden parmağını herhangi birinin üzerine koyup yukarı/
// aşağı çektiğinde hareket üstteki verticalScroll'a "kaçıp" tüm ekranı
// kaydırıyordu. Bu yardımcı, bir bileşenin üzerindeki dikey sürüklemeleri
// orada tüketip yukarı hiç yollamıyor. Düz bir dokunuş etkilenmiyor,
// çünkü detectVerticalDragGestures parmak GERÇEKTEN sürüklenmeye
// başlamadan (touch slop aşılmadan) hiçbir şey tüketmiyor. LazyRow'lu çip
// satırlarında da güvenli: bu aynı touch-slop mekanizması Compose'un
// kendi yatay/dikey kaydırma çakışması çözümüyle aynı, yani baskın yönü
// yatay olan bir sürüklemede LazyRow kendi kaydırmasını yine kazanıyor.
private fun Modifier.blockParentVerticalScrollOnDrag(): Modifier = pointerInput(Unit) {
    detectVerticalDragGestures { change, _ -> change.consume() }
}

// Kullanıcı geri bildirimi: içerikte henüz en üste gelmeden (ör. ortalarda,
// öncelik/kategori çiplerinin hizasında) aşağı doğru sürükleyip geri
// (yukarı) çıkmak istediğinde sheet bazen "en üstten aşağı çekildi"
// sanıp kendini kapatıyordu. ModalBottomSheet, Expanded durağındayken
// (sheetState.currentValue == Expanded, bizde skipPartiallyExpanded=true
// olduğu için tek "açık" durak bu) içeriğin TÜKETEMEDİĞİ her aşağı
// kaydırma artığını kendi kapatma sürüklemesine yorumluyor, normalde bu
// SADECE içerik gerçekten en üstteyken (scrollState.value == 0) olması
// gereken bir şey, ama pratikte (muhtemelen sheet'in kendi yükseklik
// ölçümünün "önce tahmin et sonra düzelt" davranışı yüzünden, bkz.
// fillMaxHeight yorumu yukarıda) bazen erken tetikleniyordu. Bu
// NestedScrollConnection, içeriğin KENDİ scrollState'i 0'da DEĞİLKEN
// gelen aşağı kaydırma artığını burada tüketip sheet'e hiç sızdırmıyor,
// sheet'i kaydırarak kapatma özelliği sadece içerik GERÇEKTEN en üstteyken
// çalışmaya devam ediyor, aksi halde kaydırma normal şekilde içeriğe
// uygulanıyor.
private fun sheetContentNestedScrollGuard(scrollState: ScrollState): NestedScrollConnection =
    object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.y > 0f && scrollState.value > 0) {
                val consumedHere = scrollState.dispatchRawDelta(available.y)
                return Offset(0f, consumedHere)
            }
            return Offset.Zero
        }
    }

// Faz 1'deki AddTaskSheet'in yerini alıyor: hem yeni görev eklemek hem de
// var olan bir görevi düzenlemek için kullanılıyor (existingTask null mu
// değil mi ona bakarak ekran kendini ona göre ayarlıyor). Alt görev listesi
// sadece existingTask doluyken görünüyor, yeni bir görevin henüz id'si
// olmadığı için alt görev eklemek anlamsız, önce başlığı kaydetmesi lazım.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    existingTask: Task?,
    categories: List<Category>,
    subtasksFlow: Flow<List<Subtask>>?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        categoryId: Long?,
        priority: Priority,
        dueAt: Long?,
        note: String?,
        recurrence: Recurrence,
    ) -> Unit,
    onAddCategory: (name: String, colorHex: String, onCreated: (Long) -> Unit) -> Unit,
    onAddSubtask: (title: String) -> Unit,
    onToggleSubtaskDone: (Subtask) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
    // Faz 12a: sık kullanılan görevler için tek dokunuşluk şablonlar.
    // Şablon listesi sadece YENİ görev eklenirken gösteriliyor (bir
    // şablonu seçmek, aşağıdaki alanları o şablonun değerleriyle
    // dolduruyor), var olan bir görevi düzenlerken şablon seçmenin
    // hiçbir anlamı yok.
    templates: List<TaskTemplateEntity> = emptyList(),
    onSaveAsTemplate: (title: String, priority: Priority, categoryId: Long?, note: String?, onSaved: () -> Unit) -> Unit = { _, _, _, _, _ -> },
    onDeleteTemplate: (TaskTemplateEntity, onDeleted: () -> Unit) -> Unit = { _, _ -> },
    // Faz 12a: doğal dil tarih ayrıştırma. Saf/senkron bir fonksiyon,
    // her tuş vuruşunda çağrılabilecek kadar ucuz (regex, veritabanı yok).
    onParseDate: (String) -> ParsedDate? = { null },
    // Faz 12b: paylaşım sayfasından ("Paylaş" > IvırByYuinn) gelen metinle
    // başlığı ÖN DOLDURMAK için, existingTask null İKEN (yeni görev)
    // anlamlı, düzenleme modunda hiç kullanılmıyor.
    initialTitle: String = "",
    // Ayarlar > Onay Mesajları'ndan kapatılabilen alt görev onayları; false
    // ise ilgili dialog hiç açılmadan işlem direkt yapılıyor.
    confirmOnSubtaskDelete: Boolean = true,
    confirmOnSubtaskComplete: Boolean = true,
) {
    val context = LocalContext.current
    // Kullanıcı geri bildirimi: özellikle "Görevi düzenle" ekranında (Alt
    // görevler bölümü yüzünden "Yeni görev"den biraz daha uzun) yukarı
    // çekince sarsılarak yerine oturuyordu, hafif aşağı çekilince de aynı
    // sarsıntı başlıyordu. Kök neden: varsayılan ModalBottomSheet'in ÜÇ
    // durağı var: Hidden / PartiallyExpanded / Expanded. İçeriğimiz zaten
    // (verticalScroll sayesinde) her zaman mümkün olduğunca büyüyüp neredeyse
    // tam ekranı kaplamak istiyor, yani PartiallyExpanded durağı bizim için
    // hiç anlamlı bir "yarı açık" durum değil, sadece Expanded'a giden yolun
    // ortasında gereksiz bir ara durak, sürükleme bu durağın YAKININDAYKEN
    // hangi durağa "yapışacağını" karara bağlarken görsel sarsıntı oluyordu.
    // skipPartiallyExpanded = true bu ara durağı tamamen kaldırıyor: artık
    // sadece Hidden ↔ Expanded var, sürüklemenin karar vermesi gereken tek
    // bir sınır kalıyor, sarsıntıya sebep olan ara-durak çakışması ortadan
    // kalkıyor.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by rememberSaveable { mutableStateOf(existingTask?.title ?: initialTitle) }

    // Faz 12c: sesli komutla görev ekleme. Kendi SpeechRecognizer
    // oturumumuzu YÖNETMİYORUZ (RECORD_AUDIO izni + mikrofon seviyesi UI'ı
    // gerektirirdi), bunun yerine sistemin KENDİ ses tanıma ekranını
    // (genelde Google uygulaması) açıyoruz, mikrofonu O yönetiyor, bize
    // sadece tanınan metni geri veriyor.
    val voiceInputUnavailableMessage = stringResource(R.string.voice_input_unavailable)
    // Şablon kaydetme/silme bu sheet'in İÇİNDE oluyor, bu yüzden diğer
    // sheet-içi mesajlarla (bkz. duplicate_task_title_message) AYNI sebeple
    // Snackbar değil Toast kullanıyoruz, sheet kendi penceresinde açık.
    val templateSavedFormat = stringResource(R.string.template_saved_snackbar)
    val templateDeletedFormat = stringResource(R.string.template_deleted_snackbar)
    val voiceInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val recognized = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (recognized != null) {
            title = recognized
        }
    }
    var selectedCategoryId by rememberSaveable { mutableStateOf(existingTask?.categoryId) }
    var priority by rememberSaveable { mutableStateOf(existingTask?.priority ?: Priority.MEDIUM) }
    var dueAt by rememberSaveable { mutableStateOf(existingTask?.dueAt) }
    var note by rememberSaveable { mutableStateOf(existingTask?.note.orEmpty()) }
    var recurrence by rememberSaveable { mutableStateOf(existingTask?.recurrence ?: Recurrence.NONE) }

    // Faz 12a: kullanıcı bu ÖNERİYİ (tam bu metin için) bir kere
    // reddettiyse (çipin "Yoksay"ına bastıysa) aynı metin için tekrar
    // göstermiyoruz, metni değiştirirse öneri yeniden değerlendirilir.
    var dismissedSuggestionFor by remember { mutableStateOf<String?>(null) }
    val parsedDate = remember(title) { if (title.isNotBlank()) onParseDate(title) else null }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var templatePendingDelete by remember { mutableStateOf<TaskTemplateEntity?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmRationale by remember { mutableStateOf(false) }
    // DatePicker'dan seçilen günü, saat seçilene kadar burada tutuyoruz.
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // verticalScroll ekli DEĞİLDİ, bu da iki gerçek soruna sebep
        // oluyordu: (1) içerik (özellikle alt görevler eklendikçe) ekran
        // boyunu aşınca en alttaki Kaydet butonuna hiç erişilemiyordu,
        // kullanıcı "kaydet butonu yok" diye şikayet etti ama aslında
        // vardı, sadece görünür alanın dışına taşıyordu; (2) ModalBottomSheet
        // kaydırılabilir bir içerik OLMADAN tam yüksekliğe yakın
        // sürüklendiğinde, sheet'in kendi genişleme/daralma animasyonuyla
        // parmak hareketi arasında bir çakışma oluşup sürekli yukarı-aşağı
        // salınıyordu (özellikle ikinci kez yukarı çekildiğinde). İçeriği
        // kaydırılabilir yapmak ikisini de çözüyor.
        //
        // Kaydet/Vazgeç butonlarını BİLEREK bu kaydırılabilir Column'un
        // dışına, sabit bir alt bölüme taşımayı denedik ama bu, sheet henüz
        // tam genişlememişken (örn. başlık alanına dokunduğun ilk an) az
        // önce yukarıda anlatılan AYNI "buton erişilemiyor" sorununu geri
        // getirdi, bu sefer kaydırarak da kurtarılamıyordu, çünkü buton
        // artık kaydırılabilir alanın dışındaydı. O yüzden butonlar burada,
        // içeride kalıyor; "üzerinde sürükleyince kaymasın" isteğini aşağıda
        // butonların KENDİ satırına eklenen bir dokunma dinleyicisiyle
        // (bkz. Row'un pointerInput'u) çözüyoruz. Sürükleme hareketini
        // orada tüketip üstteki verticalScroll'a hiç ulaşmasını engelliyoruz.
        //
        // fillMaxHeight(): kullanıcı geri bildirimi: sheet açılırken üst
        // kısım %90 anında yerine oturup son %10'u titreyerek tamamlıyordu.
        // Kök neden: ModalBottomSheet, "Expanded" durağının tam olarak NE
        // KADAR yükseklik gerektirdiğini içeriğin GERÇEK ölçümü bittikten
        // SONRA (bir/birkaç kare gecikmeyle) öğreniyor. O ana kadar tahmini
        // bir hedefe doğru animasyon zaten başlamış oluyor, gerçek yükseklik
        // netleşince hedef küçük bir miktar DÜZELTİLİYOR; bu son anki düzeltme
        // titrek/yavaş görünüyor. Bunu SubtaskSection'ın asenkron yüklenmesine
        // (alt görevler) bağlayıp animateContentSize denedik ama "Yeni görev"
        // ekranında (hiç alt görev bölümü yokken) da aynı sorun görülünce o
        // teori çürüdü, kök neden içerik dinamikliği değil, sheet'in kendi
        // "önce tahmin et, sonra düzelt" ölçüm sırası. fillMaxHeight() içeriği
        // (zaten kaydırılabilir olduğu için taşma sorunu yok) sheet'in
        // kendisine ayrılan alanın TAMAMINI baştan istemeye zorluyor, bu
        // sayede "Expanded" hedefi İLK karede, hiçbir içerik ölçümüne
        // bağlı kalmadan kesinleşiyor, sonradan düzeltilecek bir şey kalmıyor.
        val sheetScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .nestedScroll(remember(sheetScrollState) { sheetContentNestedScrollGuard(sheetScrollState) })
                .verticalScroll(sheetScrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            // Faz 10c'de burada satırdan editöre "büyüyerek gelen" gerçek bir
            // shared-element geçişi denedik (SharedTransitionLayout) ama
            // ModalBottomSheet kendi penceresinde açıldığı için ("layouts are
            // not part of the same hierarchy" çökmesi, bkz. rehber/faz10c-*.md)
            // TaskRow'un bulunduğu ana pencereyle aynı koordinat uzayını
            // paylaşamıyor, geri alındı. Bu nokta artık paylaşılan bir öğe
            // değil, sadece düzenlenen görevin GÜNCEL önceliğini (kullanıcı
            // aşağıdaki Öncelik seçiciyle değiştirdikçe canlı güncellenen)
            // gösteren sade bir görsel ipucu.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(priorityColor(priority)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(if (existingTask == null) R.string.new_task_title else R.string.edit_task_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            // Faz 12a: şablon seçmek aşağıdaki alanları dolduruyor, sadece
            // yeni görev eklerken anlamlı, sadece şablon varsa gösteriliyor.
            if (existingTask == null && templates.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.templates_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                )
                val deleteTemplateDescription = stringResource(R.string.delete_template_cd)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = templates, key = { it.id }) { template ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                title = template.title
                                priority = template.priority
                                selectedCategoryId = template.categoryId
                                note = template.note.orEmpty()
                            },
                            label = { Text(template.title) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = deleteTemplateDescription,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { templatePendingDelete = template },
                                )
                            },
                        )
                    }
                }
            }

            val voiceInputDescription = stringResource(R.string.voice_input_cd)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.task_title_field_label)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                        // Cihazda ses tanıma uygulaması yoksa launch()
                        // ActivityNotFoundException fırlatabiliyor, bu tek
                        // seferlik hata için ayrı bir Snackbar altyapısı
                        // kurmaya değmez, kısa bir Toast yeterli.
                        try {
                            voiceInputLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, voiceInputUnavailableMessage, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(imageVector = IvirIcons.Mic, contentDescription = voiceInputDescription)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            // Faz 12a: doğal dil tarih ayrıştırma. Sadece dueAt HENÜZ
            // boşken öneriyoruz, kullanıcı elle bir tarih seçtiyse buna
            // asla sessizce müdahale etmiyoruz.
            if (dueAt == null && parsedDate != null && title != dismissedSuggestionFor) {
                // Tarih+saat metni formatına göre uzayabiliyor ("Yoksay"
                // butonuyla birlikte dar ekranda taşabiliyordu), yatay
                // kaydırma ekleyerek chip'in kendi içinde bölünmesini
                // önlüyoruz.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                ) {
                    AssistChip(
                        onClick = {
                            dueAt = parsedDate.millis
                            dismissedSuggestionFor = title
                        },
                        label = {
                            Text(
                                stringResource(
                                    R.string.date_suggestion_action,
                                    dueDateTimeFormatter.format(Date(parsedDate.millis)),
                                ),
                            )
                        },
                    )
                    TextButton(onClick = { dismissedSuggestionFor = title }) {
                        Text(stringResource(R.string.date_suggestion_dismiss))
                    }
                }
            }

            Text(
                text = stringResource(R.string.priority_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            // Kategori satırıyla aynı desen: sabit bir Row yerine LazyRow,
            // dar ekranlarda veya büyük sistem yazı tipinde 3 çip yan yana
            // sığmayabiliyor, LazyRow bu durumda kırpmak/etiketi sarmak
            // yerine yatay kaydırmaya izin veriyor.
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.blockParentVerticalScrollOnDrag(),
            ) {
                items(items = Priority.entries, key = { it.name }) { option ->
                    FilterChip(
                        selected = priority == option,
                        onClick = { priority = option },
                        label = { Text(option.label()) },
                        // Kullanıcı isteğiyle: hangi rengin hangi önceliğe
                        // karşılık geldiğini seçmeden ÖNCE de görebilsin diye
                        // her çipin başında priorityColor() ile aynı renk,
                        // görev listesindeki/editördeki noktayla birebir aynı.
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(priorityColor(option)),
                            )
                        },
                    )
                }
            }

            Text(
                text = stringResource(R.string.category_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.blockParentVerticalScrollOnDrag(),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text(stringResource(R.string.option_none)) },
                    )
                }
                items(items = categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { showAddCategoryDialog = true },
                        label = { Text(stringResource(R.string.category_add_new)) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.due_date_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.blockParentVerticalScrollOnDrag(),
            ) {
                TextButton(onClick = { showDatePicker = true }) {
                    Text(
                        dueAt?.let { dueDateTimeFormatter.format(Date(it)) }
                            ?: stringResource(R.string.due_date_add),
                    )
                }
                if (dueAt != null) {
                    TextButton(onClick = { dueAt = null }) { Text(stringResource(R.string.action_clear)) }
                }
            }

            Text(
                text = stringResource(R.string.recurrence_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            // Aynı LazyRow düzeltmesi burada da lazım: bu satır tam olarak
            // kullanıcının bildirdiği hataydı: "Her ay" gibi iki kelimelik
            // etiketler, sabit Row chip'leri sıkıştırdığında ortadan bölünüp
            // ikinci kelime alt satıra düşüyordu.
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.blockParentVerticalScrollOnDrag(),
            ) {
                items(items = Recurrence.entries, key = { it.name }) { option ->
                    FilterChip(
                        selected = recurrence == option,
                        onClick = { recurrence = option },
                        label = { Text(option.label()) },
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note_label)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            if (existingTask != null && subtasksFlow != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                SubtaskSection(
                    subtasksFlow = subtasksFlow,
                    onAddSubtask = onAddSubtask,
                    onToggleSubtaskDone = onToggleSubtaskDone,
                    onDeleteSubtask = onDeleteSubtask,
                    confirmOnDelete = confirmOnSubtaskDelete,
                    confirmOnComplete = confirmOnSubtaskComplete,
                )
            }

            // Faz 12a: mevcut başlık/öncelik/kategori/not değerlerini bir
            // şablon olarak kaydetmek, ayrı, bağımsız bir aksiyon, ana
            // "Ekle/Kaydet" akışını etkilemiyor.
            TextButton(
                onClick = {
                    onSaveAsTemplate(title, priority, selectedCategoryId, note) {
                        Toast.makeText(context, String.format(templateSavedFormat, title), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .blockParentVerticalScrollOnDrag(),
            ) {
                Text(stringResource(R.string.save_as_template))
            }

            // Kullanıcı geri bildirimi: burada net bir "İptal" butonu
            // görünmüyordu (sadece sheet'i dışarıdan kapatarak/kaydırarak
            // vazgeçilebiliyordu, bu her zaman belli olmuyor), artık
            // Kaydet'in yanında açıkça duruyor. blockParentVerticalScrollOnDrag
            // için bkz. dosyanın başındaki yorum.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
                    .blockParentVerticalScrollOnDrag(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = {
                        onSave(title, selectedCategoryId, priority, dueAt, note, recurrence)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(if (existingTask == null) R.string.action_add else R.string.action_save))
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, colorHex ->
                onAddCategory(name, colorHex) { newId -> selectedCategoryId = newId }
                showAddCategoryDialog = false
            },
        )
    }

    templatePendingDelete?.let { template ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_template_title),
            message = stringResource(R.string.confirm_delete_template_message, template.title),
            onConfirm = {
                onDeleteTemplate(template) {
                    Toast.makeText(context, String.format(templateDeletedFormat, template.title), Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { templatePendingDelete = null },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueAt ?: pendingDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // DatePicker'da "bugün" sadece bir HALKA ile işaretleniyor,
                    // bu bir seçim DEĞİL (Material3'ün tasarımı böyle). Kullanıcı
                    // hiçbir güne dokunmadan doğrudan İleri'ye basarsa
                    // selectedDateMillis null kalıyordu ve son tarih sessizce
                    // hiç kaydedilmiyordu. Kullanıcı "tarih eklenemiyor" diye
                    // şikayet etti, gerçek sebep buydu. Seçim yoksa bugünü
                    // varsayıyoruz (zaten görsel olarak "seçili gibi" duruyordu).
                    pendingDateMillis = datePickerState.selectedDateMillis ?: todayAsUtcMidnightMillis()
                    showDatePicker = false
                    showTimePicker = true
                }) { Text(stringResource(R.string.action_next)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val existingCalendar = Calendar.getInstance().apply { dueAt?.let { timeInMillis = it } }
        val timePickerState = rememberTimePickerState(
            initialHour = existingCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = existingCalendar.get(Calendar.MINUTE),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueAt = combineDateAndTime(pendingDateMillis, timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                    // Kullanıcı bir son tarih/saat seçti ama tam zamanlı
                    // alarm izni yoksa (Android 12+), bunu şimdi, tam
                    // ihtiyacın olduğu anda soruyoruz, açılışta rastgele
                    // sormak yerine.
                    if (!hasExactAlarmPermission(context)) {
                        showExactAlarmRationale = true
                    }
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    if (showExactAlarmRationale) {
        AlertDialog(
            onDismissRequest = { showExactAlarmRationale = false },
            title = { Text(stringResource(R.string.exact_alarm_title)) },
            text = { Text(stringResource(R.string.exact_alarm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmRationale = false
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) { Text(stringResource(R.string.go_to_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmRationale = false }) { Text(stringResource(R.string.not_now)) }
            },
        )
    }
}

@Composable
private fun Recurrence.label(): String = when (this) {
    Recurrence.NONE -> stringResource(R.string.option_none)
    Recurrence.DAILY -> stringResource(R.string.recurrence_daily)
    Recurrence.WEEKLY -> stringResource(R.string.recurrence_weekly)
    Recurrence.MONTHLY -> stringResource(R.string.recurrence_monthly)
}

@Composable
private fun Priority.label(): String = when (this) {
    Priority.LOW -> stringResource(R.string.priority_low)
    Priority.MEDIUM -> stringResource(R.string.priority_medium)
    Priority.HIGH -> stringResource(R.string.priority_high)
}

// Locale.getDefault(): bkz. TaskRow.dueDateFormatter'daki aynı yorum.
private val dueDateTimeFormatter get() = SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault())

// DatePickerState'in seçilen tarihi UTC gece yarısı olarak vermesi Compose
// Material3'ün bilinen bir tuhaflığı (kullanıcının bulunduğu saat dilimine
// göre değil). Burada seçilen "günü" UTC takviminden okuyup, aynı gün/ay/
// yılı yerel saat dilimindeki bir takvime aktarıp saati oraya ekliyoruz,
// böylece kullanıcı ekranda hangi günü seçtiyse dueAt da o gün oluyor.
private fun combineDateAndTime(dateMillisUtc: Long?, hour: Int, minute: Int): Long? {
    if (dateMillisUtc == null) return null
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillisUtc }
    val localCalendar = Calendar.getInstance().apply {
        set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
        set(Calendar.MILLISECOND, 0)
    }
    return localCalendar.timeInMillis
}

// DatePickerState'in "seçim yok" durumundaki fallback'i: bugünün tarihini,
// DatePicker'ın kendi ürettiği formatla aynı şekilde (UTC gece yarısı) üretir
// combineDateAndTime zaten bu formatı bekliyor, tutarlılık için aynı yolu izliyoruz.
private fun todayAsUtcMidnightMillis(): Long {
    val local = Calendar.getInstance()
    val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }
    return utcMidnight.timeInMillis
}

@Composable
private fun SubtaskSection(
    subtasksFlow: Flow<List<Subtask>>,
    onAddSubtask: (String) -> Unit,
    onToggleSubtaskDone: (Subtask) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
    confirmOnDelete: Boolean = true,
    confirmOnComplete: Boolean = true,
) {
    val subtasks by subtasksFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var newSubtaskTitle by remember { mutableStateOf("") }
    var subtaskPendingDelete by remember { mutableStateOf<Subtask?>(null) }
    // 2026-08-15: kullanıcı isteğiyle tamamlama/ekleme de silme gibi
    // "emin misin" onayından geçiyor artık, checkbox/+ tıklanınca işlem
    // hemen olmuyor, onay bekleniyor.
    var subtaskPendingToggle by remember { mutableStateOf<Subtask?>(null) }
    var pendingNewSubtaskTitle by remember { mutableStateOf<String?>(null) }

    Text(text = stringResource(R.string.subtasks_label), style = MaterialTheme.typography.labelMedium)

    subtasks.forEach { subtask ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = subtask.isDone,
                onCheckedChange = {
                    if (confirmOnComplete) subtaskPendingToggle = subtask else onToggleSubtaskDone(subtask)
                },
            )
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {
                if (confirmOnDelete) subtaskPendingDelete = subtask else onDeleteSubtask(subtask)
            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_subtask_cd))
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = newSubtaskTitle,
            onValueChange = { newSubtaskTitle = it },
            label = { Text(stringResource(R.string.new_subtask_label)) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = {
            val trimmedTitle = newSubtaskTitle.trim()
            if (trimmedTitle.isNotEmpty()) pendingNewSubtaskTitle = trimmedTitle
        }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_subtask_cd))
        }
    }

    subtaskPendingDelete?.let { subtask ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_subtask_title),
            message = stringResource(R.string.confirm_delete_subtask_message, subtask.title),
            onConfirm = { onDeleteSubtask(subtask) },
            onDismiss = { subtaskPendingDelete = null },
        )
    }

    subtaskPendingToggle?.let { subtask ->
        ConfirmDialog(
            title = stringResource(
                if (subtask.isDone) R.string.confirm_uncomplete_subtask_title else R.string.confirm_complete_subtask_title,
            ),
            message = stringResource(
                if (subtask.isDone) R.string.confirm_uncomplete_subtask_message else R.string.confirm_complete_subtask_message,
                subtask.title,
            ),
            onConfirm = { onToggleSubtaskDone(subtask) },
            onDismiss = { subtaskPendingToggle = null },
        )
    }

    pendingNewSubtaskTitle?.let { title ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_add_subtask_title),
            message = stringResource(R.string.confirm_add_subtask_message, title),
            onConfirm = {
                onAddSubtask(title)
                newSubtaskTitle = ""
            },
            onDismiss = { pendingNewSubtaskTitle = null },
        )
    }
}
