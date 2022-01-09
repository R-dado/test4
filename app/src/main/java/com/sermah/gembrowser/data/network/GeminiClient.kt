package com.sermah.gembrowser.data.network

import android.net.Uri
import android.util.Log
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object GeminiClient {
    var socketFactory: SocketFactory

    init {
        // Create SocketFactory that allows self-signed certificates (disables certificate validation O_o)
        // Still, having this instead of non-ssl connection is better for future
        val context: SSLContext = SSLContext.getInstance("TLS")
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {}

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
        context.init(null, trustAllCerts, null)
        socketFactory = context.socketFactory
    }

    fun connectAndRetrieve(
        uri: Uri,
        onSuccess: (String, String) -> Unit, // header, body
        onNotSuccess: (String) -> Unit // header (in case of errors, input, redirect)
    ){
        if (uri.scheme != "gemini") return

        Thread {
            run {
                val port = if (uri.port == -1) 1965 else uri.port
                Log.d("GeminiClient", "Connecting to ${uri.host} at port $port...")
                try{
                    val client = socketFactory.createSocket(uri.host, port)
                    client.soTimeout = 10*1000
                    Log.d("GeminiClient", "Connected = ${client.isConnected}")
                    val output = OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)
                    val input = Scanner(client.inputStream)

                    Log.d("GeminiClient", "Sending: \"$uri\"")
                    output.write(uri.toString()+"\r\n")
                    output.flush()

                    var header: String = "20 header"
                    //Log.d("GeminiClient", input.hasNext().toString())
                    if (input.hasNext())
                        header = input.nextLine()// TODO: Limit meta to 1024 bytes
                    Log.d("GeminiClient", "Received header: $header")
                    var body = ""
                    if (header[0] == '2' && header.startsWith("text", 3,  true)) {
                        while (client.isConnected && input.hasNext()) {
                            body += input.nextLine() + "\n"
                        }
                    }
                    client.close()

                    if (header[0] == '2') onSuccess(header, body)
                    else onNotSuccess(header)

                    client.close()
                } catch (e : ConnectException) {
                    Log.d("Gemini Client", "Connection failed")
                } catch (e: SocketTimeoutException) {
                    Log.d("Gemini Client", "Connection timed out")
                } catch (e: Exception) {
                    Log.d("Gemini Client", "Other: $e")
                }

            }
        }.start()
    }

}