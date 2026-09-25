import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.subscription.manager.App

fun main() = application {
    val windowState = rememberWindowState(width = 440.dp, height = 880.dp)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Renewo"
    ) {
        App()
    }
}
