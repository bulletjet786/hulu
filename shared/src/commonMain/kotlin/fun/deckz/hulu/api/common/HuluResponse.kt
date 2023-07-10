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

        fun success(): HuluResponse<Nothing> {
            return HuluResponse();
        }

        fun failure(code: Int, msg: String): HuluResponse<Nothing> {
            return HuluResponse(status = Status(code, msg))
        }

    }


}
