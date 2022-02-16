package com.sermah.gembrowser.data.content

data class GeminiResponse (
    val header: String,
    val body: String = ""
) : IResponse {

    val status: String
        get() = header.substring(0..1)

    val meta: String
        get() = header.substring(3)

    override fun toString(): String {
        return StringBuilder()
            .append("Status: `").append(status)
            .append("`; Meta: `").append(meta)
            .append("`; Body: ").append(body.length).append(" bytes.")
            .toString()
    }

    override fun toFullString(): String {
        return StringBuilder()
            .append("=== Header ===\n")
            .append("Status: `").append(status)
            .append("`; Meta: `").append(meta)
            .append("`\n=== Body ===\n").append(body).append("\n=== End ===")
            .toString()
    }
}