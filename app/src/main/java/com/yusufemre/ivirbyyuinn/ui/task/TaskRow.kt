package com.yusufemre.ivirbyyuinn.ui.task

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.data.Category
import com.yusufemre.ivirbyyuinn.data.Priority
import com.yusufemre.ivirbyyuinn.data.Recurrence
import com.yusufemre.ivirbyyuinn.data.Subtask
import com.yusufemre.ivirbyyuinn.data.Task
import com.yusufemre.ivirbyyuinn.ui.theme.IvirIcons
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDeleteDialog
import com.yusufemre.ivirbyyuinn.ui.util.ConfirmDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Öncelik rengini, Priority enum'undan Compose'un anlayacağı bir Color'a
// çeviren küçük bir yardımcı. Faz 10c'den beri TaskEditorSheet.kt de aynı
// rengi kullandığı için (görev başlığının yanındaki öncelik noktası) artık
// "private" değil, aynı paket içinde paylaşılıyor, ayrı bir dosyaya
// taşımaya gerek olmayacak kadar küçük bir fonksiyon olduğu için.
//
// 2026-08-09: kullanıcı isteğiyle düşükten yükseğe mavi→yeşil→kırmızı
// (trafik ışığı mantığına yakın, "önem arttıkça sıcaklaşan" sezgisel bir
// sıralama), önceki gri/amber/mercan üçlüsünün yerine. Uygulamanın genel
// estetiğiyle (23 renk temasının seed'leri, StreakFlameColor) tutarlı
// olsun diye burada da saf/neon (#0000FF vb.) değil, tonu kırılmış
// (muted) versiyonlar seçildi.
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> Color(0xFF4A7FB5)
    Priority.MEDIUM -> Color(0xFF4F9153)
    Priority.HIGH -> Color(0xFFC94F4F)
}

// Locale.getDefault(), MainActivity.attachBaseContext'in ayarladığı dile
// göre değişiyor, kullanıcı dili değiştirip Activity yeniden kurulunca
// (recreate) bu da güncel dile göre yeniden oluşuyor.
private val dueDateFormatter get() = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())

// "↻" karakteriyle tekrarlı görevleri gözle ayırt edilebilir kılıyoruz,
// ayrı bir ikon paketine gerek kalmadan. buildList{} @Composable OLMAYAN
// bir kapsam olduğu için (bkz. TaskRow içindeki kullanım), stringResource
// çağrılarını burada değil, TaskRow'un composable gövdesinde önceden
// çözüyoruz. Bu fonksiyon sadece hazır string'ler arasından seçiyor.
private fun Recurrence.shortLabel(daily: String, weekly: String, monthly: String): String? = when (this) {
    Recurrence.NONE -> null
    Recurrence.DAILY -> daily
    Recurrence.WEEKLY -> weekly
    Recurrence.MONTHLY -> monthly
}

// Listedeki tek bir satır: öncelik rengi, checkbox, başlık (+ kategori/son
// tarih/alt görev sayacı alt satırda), sil butonu. Tamamlanan görevi üstü
// çizili ve soluk renkte gösteriyoruz ki gözle bile hemen ayırt edilebilsin.
// Satırın kendisine dokunmak (checkbox/sil hariç) düzenleme ekranını açıyor.
//
// Faz 4a'da SwipeToDismissBox eklendi: sağa kaydırmak tamamla/geri al,
// sola kaydırmak çöp kutusuna taşı. Checkbox ve sil butonu KALKMADI,
// swipe sadece bir kısayol, roadmap'teki "her gesture'ın buton
// alternatifi olmalı" kuralına uygun.
//
// Faz 10a'da satır düz bir Row+background'dan hafif tonal bir Card'a
// dönüştü, sol kenara öncelik renginde ince bir şerit eklendi (Ece'nin
// önerisi), önceki 10dp'lik nokta da kaldı, ikisi birlikte "kartın kendisi
// önceliğin rengini taşıyor" hissi veriyor).
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: Task,
    category: Category?,
    // 2026-08-15: kullanıcı isteğiyle, alt görevler görevin altında ufak
    // ufak listeleniyor ve buradan da tamamlanıp geri alınabiliyor,
    // ana görevle aynı onay diyaloğu deseniyle (bkz. SubtaskMiniRow).
    // Silme hâlâ sadece düzenleme ekranında (TaskEditorSheet).
    subtasks: List<Subtask> = emptyList(),
    onToggleSubtaskDone: (Subtask) -> Unit = {},
    onToggleDone: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onArchive: (Task) -> Unit = {},
    onClick: (Task) -> Unit,
    // Kullanıcı Ayarlar > Onay Mesajları'ndan bu onayları tek tek
    // kapatabiliyor; false ise dialog hiç açılmadan işlem direkt yapılıyor.
    confirmOnDelete: Boolean = true,
    confirmOnComplete: Boolean = true,
    confirmOnArchive: Boolean = true,
    confirmOnSubtaskComplete: Boolean = true,
    modifier: Modifier = Modifier,
    // Sadece SortOption.MANUAL seçiliyken dolu geliyor, TaskListScreen
    // buraya sürükleme algılamasını (pointerInput) zaten kurulu olarak
    // veriyor, TaskRow'un sürükleme mantığını bilmesine gerek yok,
    // sadece "buraya bir tutamaç ikonu koy" diyoruz.
    dragHandleModifier: Modifier? = null,
    // Faz 12c: toplu seçim. selectionMode aktifken checkbox isDone yerine
    // isSelected'ı gösteriyor, swipe (tamamla/sil kısayolu) kapanıyor,
    // seçim yaparken yanlışlıkla bir görevi tamamlamak/silmek istemiyoruz.
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (Task) -> Unit = {},
    onLongClick: (Task) -> Unit = {},
) {
    // Faz 4b'de eklendi: tamamlama/silme anlarında ince bir titreşim,
    // parmağın altında "bir şey oldu" hissi veriyor, özellikle swipe
    // gibi görsel geri bildirimin göz ucuyla kaçabileceği durumlarda işe
    // yarıyor.
    val haptics = LocalHapticFeedback.current
    val reducedMotion = rememberReducedMotionEnabled()

    val recurrenceDaily = stringResource(R.string.recurrence_short_daily)
    val recurrenceWeekly = stringResource(R.string.recurrence_short_weekly)
    val recurrenceMonthly = stringResource(R.string.recurrence_short_monthly)
    val deleteTaskDescription = stringResource(R.string.delete_task_cd)
    val archiveTaskDescription = stringResource(R.string.archive_task_cd)
    val dragHandleDescription = stringResource(R.string.drag_handle_cd)

    // 2026-08-09: Kullanıcı isteğiyle silme VE tamamlama/tamamlamama artık
    // HER yoldan (sil butonu, sağa/sola kaydırma, checkbox) aynı onay
    // adımından geçiyor, önceden swipe-ile-sil bilerek bu onaya tabi
    // değildi (zaten geri alınabilir bir "Geri Al" snackbar'ı vardı), ama
    // kullanıcı artık swipe için de onay istiyor.
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showToggleConfirm by remember { mutableStateOf(false) }
    // 2026-08-11: arşivleme de aynı onay adımından geçiyor, kullanıcı
    // isteğiyle (geri alınabilir bir işlem olsa da, yanlışlıkla basılmasın).
    var showArchiveConfirm by remember { mutableStateOf(false) }

    // confirmOnDelete/confirmOnComplete false ise onay diyaloğu hiç
    // açılmadan işlem direkt tetikleniyor; silme/tamamlamanın HER giriş
    // noktası (sil butonu, swipe, checkbox) bu iki fonksiyonu çağırıyor,
    // davranış tek bir yerden kontrol ediliyor.
    val requestDelete = {
        if (confirmOnDelete) showDeleteConfirm = true else onDelete(task)
    }
    val requestToggle = {
        if (confirmOnComplete) showToggleConfirm = true else onToggleDone(task)
    }
    val requestArchive = {
        if (confirmOnArchive) showArchiveConfirm = true else onArchive(task)
    }

    // confirmValueChange bu Compose sürümünde deprecated işaretlenmiş
    // (yerine "dynamic anchors" tabanlı yeni bir API öneriliyor, henüz
    // dokümantasyonu/örnekleri olgunlaşmamış). Çalışması hâlâ doğru ve
    // güvenli olduğu için şimdilik bunu kullanmaya devam ediyoruz, API
    // netleşince güncelleriz.
    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Sağa kaydırma: tamamla/geri al. Artık işlemi hemen
                    // yapmıyoruz, onay dialoğunu açıyoruz, satır her
                    // zaman yerine geri dönüyor (false), gerçek değişiklik
                    // sadece kullanıcı onaylarsa oluyor.
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    requestToggle()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Sola kaydırma: çöp kutusuna taşı. Artık burada da
                    // hemen silmiyoruz, onay dialoğunu açıyoruz ve satırı
                    // yerine geri döndürüyoruz (false); kullanıcı
                    // onaylarsa onDelete gerçek silmeyi tetikleyip satırı
                    // normal liste güncellemesiyle (animateItem) kaldırıyor.
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    requestDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    // Faz 10a: kullanıcı eşiği (yarı yol) GEÇERKEN de bir haptic hissetsin
    // istedik, sadece işlem gerçekleştiğinde değil. Swipe'ın "artık
    // tetiklenecek" anını, parmağını kaldırmadan ÖNCE fark etsin diye.
    // dismissState.progress her piksel hareketinde değiştiği için normal
    // bir LaunchedEffect(dismissState.progress) her karede coroutine'i
    // yeniden başlatırdı; snapshotFlow tek bir uzun ömürlü coroutine
    // açıp değişiklikleri bir akış gibi dinliyor, çok daha ucuz.
    LaunchedEffect(dismissState) {
        var crossedThreshold = false
        snapshotFlow { dismissState.targetValue to dismissState.progress }
            .collect { (target, progress) ->
                if (target == SwipeToDismissBoxValue.Settled) {
                    crossedThreshold = false
                } else if (progress > 0.5f && !crossedThreshold) {
                    crossedThreshold = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                } else if (progress <= 0.5f) {
                    crossedThreshold = false
                }
            }
    }

    // Faz 10a: görev tamamlanınca (SADECE false->true yönünde, geri almak
    // sakin kalıyor) kartın tamamı çok kısa bir "pop" yapıyor,
    // Motion.emphasizedSpring ile abartısız bir kutlama hissi. Kullanıcının
    // cihazda "animasyonları azalt" ayarı açıksa bu adım tamamen atlanıyor,
    // sadece aşağıdaki renk geçişi kalıyor.
    val popScale = remember { Animatable(1f) }
    LaunchedEffect(task.id, task.isDone) {
        if (task.isDone && !reducedMotion) {
            popScale.animateTo(1.05f, animationSpec = tween(Motion.MICRO))
            popScale.animateTo(1f, animationSpec = Motion.emphasizedSpring())
        }
    }

    // Faz 12c: seçim modunda swipe/sürükleme/tekli-sil hiçbiri yok, sadece
    // "seçili mi" gösteren sade bir kart. Erken dönüş sayesinde normal mod
    // (SwipeToDismissBox + sürükleme desteği) hiç kurulmuyor bile.
    if (selectionMode) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { onToggleSelect(task) },
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect(task) })
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(priorityColor(task.priority)),
                )
                TaskTitleColumn(
                    task = task,
                    category = category,
                    subtasks = subtasks,
                    onToggleSubtaskDone = onToggleSubtaskDone,
                    recurrenceDaily = recurrenceDaily,
                    recurrenceWeekly = recurrenceWeekly,
                    recurrenceMonthly = recurrenceMonthly,
                    confirmOnSubtaskComplete = confirmOnSubtaskComplete,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
            }
        }
        return
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { SwipeBackground(dismissState.targetValue, dismissState.progress) },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(popScale.value)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            // IntrinsicSize.Min: bu Row'un yüksekliği içeriğine göre
            // belirleniyor (wrap content), ama öncelik şeridinin
            // fillMaxHeight() ile o yüksekliğe TAM oturması için Row'a
            // "kendi doğal yüksekliğini ölç" demek gerekiyor, aksi halde
            // fillMaxHeight'ın referans alacağı bir üst sınır olmazdı.
            Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(priorityColor(task.priority)),
                )
                if (dragHandleModifier != null) {
                    // 2026-08-10: tutamaç artık combinedClickable'lı satırın
                    // İÇİNDE değil, ona bitişik bir KARDEŞ öğe, böylece
                    // parmağın ilk bastığı nokta tutamacın üstündeyse, o dokunuş
                    // hiç satırın "uzun basınca seç" algılayıcısına uğramıyor,
                    // sadece aşağıdaki sürükleme jestine gidiyor. Önceden ikisi
                    // aynı Row'un (aynı hit-test alanının) içinde olduğu için
                    // hangisinin kazanacağı bir yarışa kalıyordu; şimdi ikisinin
                    // dokunma alanları hiç çakışmıyor, yarış diye bir şey yok.
                    Icon(
                        imageVector = IvirIcons.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragHandleModifier
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                            .semantics { contentDescription = dragHandleDescription },
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        // Faz 12c: normal tıklama düzenlemeyi açmaya devam
                        // ediyor, long-press YENİ bir seçim modunu başlatıyor.
                        // Tutamaç artık bu Row'un dışında olduğu için, buradaki
                        // long-click sadece satırın gerçek gövdesine basılınca
                        // tetikleniyor.
                        .combinedClickable(
                            onClick = { onClick(task) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick(task)
                            },
                        )
                        .padding(
                            start = if (dragHandleModifier != null) 0.dp else 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            requestToggle()
                        },
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(priorityColor(task.priority)),
                    )
                    TaskTitleColumn(
                        task = task,
                        category = category,
                        subtasks = subtasks,
                        onToggleSubtaskDone = onToggleSubtaskDone,
                        recurrenceDaily = recurrenceDaily,
                        recurrenceWeekly = recurrenceWeekly,
                        recurrenceMonthly = recurrenceMonthly,
                        confirmOnSubtaskComplete = confirmOnSubtaskComplete,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                    // Kullanıcı isteği: görev tamamlandı işaretlenince sil
                    // butonunun SOLUNA bir arşivle butonu geliyor, Row'a
                    // eklenen bu ekstra eleman zaten sil butonunu sağa itiyor,
                    // ayrı bir kaydırma/offset hesabı gerekmiyor.
                    if (task.isDone) {
                        IconButton(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            requestArchive()
                        }) {
                            Icon(
                                imageVector = IvirIcons.Archive,
                                contentDescription = archiveTaskDescription,
                            )
                        }
                    }
                    IconButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        requestDelete()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = deleteTaskDescription,
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_task_title),
            message = stringResource(R.string.confirm_delete_task_message, task.title),
            onConfirm = { onDelete(task) },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    if (showToggleConfirm) {
        // Dialog açıldığı anda task.isDone HANGİ yöne gideceğimizi
        // belirliyor. Dialog açıkken task değişmeyeceği için (onConfirm
        // sadece onToggleDone'ı tetikliyor, task parametresi burada hâlâ
        // eski hali) başlık/mesaj metni tutarlı kalıyor.
        ConfirmDialog(
            title = stringResource(
                if (task.isDone) R.string.confirm_uncomplete_title else R.string.confirm_complete_title,
            ),
            message = stringResource(
                if (task.isDone) R.string.confirm_uncomplete_message else R.string.confirm_complete_message,
                task.title,
            ),
            onConfirm = { onToggleDone(task) },
            onDismiss = { showToggleConfirm = false },
        )
    }

    if (showArchiveConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_archive_task_title),
            message = stringResource(R.string.confirm_archive_task_message, task.title),
            onConfirm = { onArchive(task) },
            onDismiss = { showArchiveConfirm = false },
        )
    }
}

// Başlık + alt satır (kategori/son tarih/tekrar) + alt görevlerin küçük
// listesi, normal ve seçim modunda AYNI, tek bir yerde tanımlı.
@Composable
private fun TaskTitleColumn(
    task: Task,
    category: Category?,
    subtasks: List<Subtask>,
    onToggleSubtaskDone: (Subtask) -> Unit,
    recurrenceDaily: String,
    recurrenceWeekly: String,
    recurrenceMonthly: String,
    confirmOnSubtaskComplete: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Renk değişimini animateColorAsState ile yumuşatıyoruz,
        // tamamlanınca yazı aniden değil, kısa bir geçişle soluyor.
        val titleColor by animateColorAsState(
            targetValue = if (task.isDone) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            label = "task-title-color",
        )
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
            color = titleColor,
        )
        val sublineParts = buildList {
            category?.let { add(it.name) }
            task.dueAt?.let { add(dueDateFormatter.format(Date(it))) }
            task.recurrence.shortLabel(recurrenceDaily, recurrenceWeekly, recurrenceMonthly)?.let { add(it) }
        }
        if (sublineParts.isNotEmpty()) {
            Text(
                text = sublineParts.joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (subtasks.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 2.dp)) {
                subtasks.forEach { subtask ->
                    SubtaskMiniRow(
                        subtask = subtask,
                        onToggleDone = onToggleSubtaskDone,
                        confirmOnComplete = confirmOnSubtaskComplete,
                    )
                }
            }
        }
    }
}

// Alt görevin sayaçtan ("2/5") ayrı olarak kendi başlığıyla ufak bir satırda
// görünmesi, solundaki işaretçi, ana görevin tamamlama checkbox'ıyla
// BİREBİR aynı Checkbox composable'ı (2026-08-15: önce daire, sonra dolu
// kare, sonra elle çizilmiş tik denendi, kullanıcı "tamamlandığı zamankiyle
// aynı olsun" deyince gerçek Checkbox'a geçildi). Checkbox'un kendi
// dokunma alanı normalde ~40dp, bu ufak listede çok büyük durduğu için
// (kullanıcı geri bildirimi) sabit küçük bir Box'a sarıp scale() ile
// küçültüyoruz; Modifier.size ile doğrudan küçültmek Checkbox'un iç
// çizimini kırpardı, scale() aynı çizimi orantılı küçültüyor.
//
// 2026-08-15: bu satır artık tıklanabilir, ana ekrandan da alt görev
// tamamlanabiliyor, düzenleme ekranına girmeye gerek yok. Ana görevle AYNI
// onay deseni: checkbox'a dokununca hemen işlem olmuyor, "emin misin"
// diyaloğu açılıyor.
@Composable
private fun SubtaskMiniRow(subtask: Subtask, onToggleDone: (Subtask) -> Unit, confirmOnComplete: Boolean = true) {
    val haptics = LocalHapticFeedback.current
    var showToggleConfirm by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Checkbox(
                checked = subtask.isDone,
                onCheckedChange = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (confirmOnComplete) showToggleConfirm = true else onToggleDone(subtask)
                },
                modifier = Modifier.scale(0.6f),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.labelSmall,
            textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }

    if (showToggleConfirm) {
        ConfirmDialog(
            title = stringResource(
                if (subtask.isDone) R.string.confirm_uncomplete_subtask_title else R.string.confirm_complete_subtask_title,
            ),
            message = stringResource(
                if (subtask.isDone) R.string.confirm_uncomplete_subtask_message else R.string.confirm_complete_subtask_message,
                subtask.title,
            ),
            onConfirm = { onToggleDone(subtask) },
            onDismiss = { showToggleConfirm = false },
        )
    }
}

// Swipe sırasında satırın ARKASINDA görünen renkli şerit + ikon. Hangi
// yöne kaydırıldığına göre yeşil (tamamla) ya da kırmızımsı (sil) renk
// ve ikon gösteriyoruz. Faz 10a'dan beri `progress` (0f-1f, kaydırma
// mesafesi) da geliyor. Arka planın opaklığı ve ikonun boyutu artık
// sabit değil, parmağın ne kadar kaydırdığına göre büyüyor/koyulaşıyor,
// kullanıcı işlemin tetikleneceğini eylem tamamlanmadan hisssetsin diye.
@Composable
private fun SwipeBackground(targetValue: SwipeToDismissBoxValue, progress: Float) {
    val (color, icon, alignment) = when (targetValue) {
        SwipeToDismissBoxValue.StartToEnd ->
            Triple(Color(0xFF2F5D50), Icons.Default.Check, Alignment.CenterStart)
        SwipeToDismissBoxValue.EndToStart ->
            Triple(Color(0xFFE8735C), Icons.Default.Delete, Alignment.CenterEnd)
        SwipeToDismissBoxValue.Settled ->
            Triple(Color.Transparent, null, Alignment.Center)
    }
    val clampedProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (icon != null) color.copy(alpha = 0.3f + 0.7f * clampedProgress) else color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((18 + 10 * clampedProgress).dp),
            )
        }
    }
}
