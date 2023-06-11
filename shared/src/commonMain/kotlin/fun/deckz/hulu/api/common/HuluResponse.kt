package `fun`.deckz.hulu.api.common

import kotlinx.serialization.Serializable

@Serializable
class HuluResponse<T> private constructor(
    val status: Status = Status(),
    val data: T? = null
) {

    @Serializable
    class Status(
        val code: Int = 0,
        val msg: String = "Success"
    )

    companion object {
        fun <T> of(data: T): HuluResponse<T> {
            return HuluResponse(data = data);
        }

        fun <T> success(): HuluResponse<T> {
            return HuluResponse();
        }

        fun <T> failure(code: Int, msg: String): HuluResponse<T> {
            return HuluResponse(status = Status(code, msg))
        }

    }


}
