import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context

class AttachmentViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttachmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttachmentViewModel(context.applicationContext) as T
        }
        error("Unknown ViewModel class")
    }
}