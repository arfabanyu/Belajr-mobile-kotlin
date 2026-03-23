import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Message(
    val id: Long? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    val content: String? = null,
    @SerialName("attachment_url")
    val attachmentUrl: String? = null,
    @SerialName("sent_at")
    val sentAt: String? = null
)

data class ChatRoom(
    val friend: PartnerResult,
    val lastMessage: Message? = null
)