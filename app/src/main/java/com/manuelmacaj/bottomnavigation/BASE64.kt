package com.manuelmacaj.bottomnavigation

import java.util.*

//fonte: https://www.geeksforgeeks.org/basic-type-base64-encoding-and-decoding-in-java/
class BASE64() {
    fun encrypt(passwordUser: String): String? {
        return Base64.getEncoder().encodeToString(passwordUser.toByteArray())
    }

    fun decrypt(pwdEncripted: String): String {
        return String(Base64.getDecoder().decode(pwdEncripted))
    }
}