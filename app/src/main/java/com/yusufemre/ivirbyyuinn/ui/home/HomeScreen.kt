package com.yusufemre.ivirbyyuinn.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufemre.ivirbyyuinn.IvirApplication
import com.yusufemre.ivirbyyuinn.R
import com.yusufemre.ivirbyyuinn.ui.settings.SettingsScreen
import com.yusufemre.ivirbyyuinn.ui.theme.Motion
import com.yusufemre.ivirbyyuinn.ui.theme.rememberReducedMotionEnabled
import com.yusufemre.ivirbyyuinn.ui.util.AnimatedDialog
import com.yusufemre.ivirbyyuinn.ui.util.findActivity

// Uygulama süreç ilk açıldığında (soğuk başlangıç) gösterilen giriş
// ekranı: MainActivity bunu sadece BİR KERE gösteriyor, arka plandan
// her dönüşte değil (o zaten AppLockScreen'in işi, bkz. Faz 7a).
// Widget'ın "+" butonundan geldiysek bu ekran tamamen atlanıyor.
@Composable
fun HomeScreen(onEnterApp: () -> Unit) {
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Faz 10b: HomeScreen artık kendi (küçük, sadece bunun için var olan)
    // ViewModel'ini kuruyor, TaskListScreen'deki dev TaskViewModel'i
    // buraya taşımıyoruz, sadece "kaç görev seni bekliyor" bilgisine
    // ihtiyacımız var.
    val application = LocalContext.current.applicationContext as IvirApplication
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            getTaskListsUseCase = application.getTaskListsUseCase,
            getTasksUseCase = application.getTasksUseCase,
        ),
    )
    val activeTaskCount by homeViewModel.activeTaskCount.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            HomeLogo()
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            // Bekleyen görev sayısı henüz yüklenmediyse (null) ya da 0'sa
            // bu satırı hiç göstermiyoruz, "0 görev seni bekliyor" gibi
            // tuhaf/anlamsız bir cümle kurmaktansa sessiz kalmayı
            // tercih ettik.
            val count = activeTaskCount
            AnimatedVisibility(visible = count != null && count > 0) {
                if (count != null && count > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.home_active_tasks_summary, count, count),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            Button(onClick = onEnterApp, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_enter_app))
            }
            OutlinedButton(
                onClick = { showSettings = true },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.switcher_settings))
            }
            TextButton(
                onClick = { context.findActivity()?.finish() },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.home_exit))
            }
        }
    }

    if (showSettings) {
        AnimatedDialog(onDismissRequest = { showSettings = false }) { dismiss ->
            SettingsScreen(onClose = dismiss)
        }
    }
}

// Uygulama ikonuyla aynı görsel dili tekrar kullanan, elden çizilmiş
// (ekstra ikon/illüstrasyon paketi yok) küçük bir amblem: Canvas
// kullanımı EmptyTaskList/WeeklyBarChart'takiyle aynı desen.
//
// Faz 10b: HomeScreen sadece süreç başına BİR KERE görüldüğü için, burada
// biraz daha belirgin bir giriş anı hak ediyor, rozet önce hafifçe
// büyüyerek "pop" yapıyor, ARDINDAN içindeki tik işareti uçtan uca
// çizilerek beliriyor (PathMeasure ile yolun sadece bir kısmını çizmek).
@Composable
private fun HomeLogo() {
    val badgeColor = MaterialTheme.colorScheme.primary
    val checkColor = MaterialTheme.colorScheme.onPrimary
    val reducedMotion = rememberReducedMotionEnabled()

    val badgeScale = remember { Animatable(if (reducedMotion) 1f else 0.6f) }
    val checkProgress = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            badgeScale.animateTo(1f, animationSpec = Motion.emphasizedSpring())
            checkProgress.animateTo(1f, animationSpec = tween(Motion.SCREEN))
        }
    }

    Canvas(modifier = Modifier.size(88.dp).scale(badgeScale.value)) {
        drawCircle(color = badgeColor)
        val strokeWidth = 7.dp.toPx()
        val checkPath = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.54f)
            lineTo(size.width * 0.44f, size.height * 0.70f)
            lineTo(size.width * 0.74f, size.height * 0.36f)
        }
        // Tik işaretinin sadece "şu ana kadar çizilmiş" kısmını almak için
        // PathMeasure kullanıyoruz: tam yolun uzunluğunu ölçüp, 0'dan
        // checkProgress kadarlık bir dilimini yeni bir Path'e kopyalıyoruz.
        val measure = PathMeasure()
        measure.setPath(checkPath, forceClosed = false)
        val animatedCheck = Path()
        measure.getSegment(0f, measure.length * checkProgress.value, animatedCheck, startWithMoveTo = true)
        drawPath(
            path = animatedCheck,
            color = checkColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
