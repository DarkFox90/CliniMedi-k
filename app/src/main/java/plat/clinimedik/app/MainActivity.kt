
package plat.clinimedik.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import plat.clinimedik.app.ui.navigation.CliniMedikNavHost
import plat.clinimedik.app.ui.theme.CliniMedikTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CliniMedikTheme {
                CliniMedikNavHost()
            }
        }
    }
}