package `fun`.deckz.hulu.api.common

import kotlinx.serialization.Serializable

@Serializable
class HuluResponse<T> private constructor(
    val status: Status = Status.Success,
    val data: T? = null
) {

    @Serializable
    class Status(
        val code: Int = 0,
        val msg: String = ""
    ) {
        companion object {
            val Success = Status(code = 0, msg = "Success")
        }
    }

    companion object {
        fun <T> of(data: T): HuluResponse<T> {
            return HuluResponse<T>(data = data);
        }

        fun <T> success(): HuluResponse<T> {
            return HuluResponse();
        }

        fun <T> failure(code: Int, msg: String): HuluResponse<T> {
            return HuluResponse(status = Status(code, msg))
        }

    }


}
