package com.jgardo.skypebot.notification.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64
import com.nimbusds.jose.util.X509CertChainUtils
import com.nimbusds.jwt.SignedJWT
import org.junit.After
import org.junit.Before
import org.junit.Test


class AuthorizationTest {

    val validToken = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IktwSVdSVWxnZmlObGQxRFR4WkFoZTRpTm1rQSIsInR5cCI6IkpXVCIsIng1dCI6IktwSVdSVWxnZmlObGQxRFR4WkFoZTRpTm1rQSJ9.eyJzZXJ2aWNldXJsIjoiaHR0cHM6Ly9zbWJhLnRyYWZmaWNtYW5hZ2VyLm5ldC9hcGlzLyIsIm5iZiI6MTUyNDIxNDY2MSwiZXhwIjoxNTI0MjE4MjYxLCJpc3MiOiJodHRwczovL2FwaS5ib3RmcmFtZXdvcmsuY29tIiwiYXVkIjoiN2FjYjQ1NzgtYTc3Mi00YTg3LWIwMjMtNzQ4MzhhNmYxMDI4In0.VHTnwEebd1ka7m1jOZNv9ooLfayatCvsqGKZE_2PapA8lV5_FHD92Bt1pJXKR116xE5tuqCCl5W1as7TJ1ktA4Bxej4ENHZ_mhl9tO6ZsislEo1PfoVD5WDK_8U9pEt5dhbGKqddni8vYkwTa_hgebVG9gUwE4nXNn53oQuUvtQKJ9c1Kl53bEgEXa6C46UH1AifZGjT5wGJcEq7fBkE4mRGvPrukljBMuIPJyv5tg9OvCRVIcyFGUKf_ssaxIvsjmoZLP5Ax1NZxdz_9WmzctCAY8dTHo9o__ao1bivVHoyPsrzbWGy0DzfefhLHPKFZ7jTbBKwF0BxvW4m5ZvKvg"
    val key = ""

    val response = "{\n" +
            "    \"keys\": [\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"KpIWRUlgfiNld1DTxZAhe4iNmkA\",\n" +
            "            \"x5t\": \"KpIWRUlgfiNld1DTxZAhe4iNmkA\",\n" +
            "            \"n\": \"nYgTwjTExtidhEIT5D4Fu5E7ZW2ixSZPxlVj5ahX3f0vcwUIOsQkDF0MUwfovJbcl8R3RfzPO6U1BMIsVWcS1SbwG0TY-Wc9IY_coXrbmwCkxnSA0hbuE_yvsZJOv170iDuYZtsVY9Ro_hEvgEIhidSZm84GSqsoXyKc3oAcf8WRbwLEGOhkOZLZQb3mfXYPd4JoOSoea6NN2JSyTIVDzIdmA-Xtlh_zro-0vuj1YqUFuFq4V1skEiybe9tHDxJoiDaXl6P6KyU2BoTr2aQ7dTzL9gL6ZKsHt88j6H4C3V0W2uJyqVC0rYEAublK6Txni0eE4qfNiO_WyMiJJLoafw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"MIIFATCCA+mgAwIBAgITVgBVcd4AGgVX4tZeNAAAAFVx3jANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwpNU0lUIENBIFoyMB4XDTE4MDMyNzE2NDE0MloXDTIwMDMxNjE2NDE0MlowITEfMB0GA1UEAxMWYm90YXBpLXByb2Qtand0c2lnbmluZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJ2IE8I0xMbYnYRCE+Q+BbuRO2VtosUmT8ZVY+WoV939L3MFCDrEJAxdDFMH6LyW3JfEd0X8zzulNQTCLFVnEtUm8BtE2PlnPSGP3KF625sApMZ0gNIW7hP8r7GSTr9e9Ig7mGbbFWPUaP4RL4BCIYnUmZvOBkqrKF8inN6AHH/FkW8CxBjoZDmS2UG95n12D3eCaDkqHmujTdiUskyFQ8yHZgPl7ZYf866PtL7o9WKlBbhauFdbJBIsm3vbRw8SaIg2l5ej+islNgaE69mkO3U8y/YC+mSrB7fPI+h+At1dFtricqlQtK2BALm5Suk8Z4tHhOKnzYjv1sjIiSS6Gn8CAwEAAaOCAjwwggI4MCcGCSsGAQQBgjcVCgQaMBgwCgYIKwYBBQUHAwEwCgYIKwYBBQUHAwIwPgYJKwYBBAGCNxUHBDEwLwYnKwYBBAGCNxUIh9qGdYPu2QGCyYUbgbWeYYX062CBXYXe6WuB1IVnAgFkAgEUMIGFBggrBgEFBQcBAQR5MHcwMQYIKwYBBQUHMAKGJWh0dHA6Ly9jb3JwcGtpL2FpYS9NU0lUJTIwQ0ElMjBaMi5jcnQwQgYIKwYBBQUHMAKGNmh0dHA6Ly93d3cubWljcm9zb2Z0LmNvbS9wa2kvbXNjb3JwL01TSVQlMjBDQSUyMFoyLmNydDAdBgNVHQ4EFgQUboDbrLrIoB9t2Ytg0VYybC8UqLowCwYDVR0PBAQDAgWgMCEGA1UdEQQaMBiCFmJvdGFwaS1wcm9kLWp3dHNpZ25pbmcwgbUGA1UdHwSBrTCBqjCBp6CBpKCBoYYlaHR0cDovL2NvcnBwa2kvY3JsL01TSVQlMjBDQSUyMFoyLmNybIY8aHR0cDovL21zY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvTVNJVCUyMENBJTIwWjIuY3JshjpodHRwOi8vY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvTVNJVCUyMENBJTIwWjIuY3JsMB8GA1UdIwQYMBaAFGHLu4ZhQWMy1Vtmxo63nE0AbwT5MB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAkctoOWU2lj93P/yYTZSaeE9HEyW+glvXp889XV7XYSZnNwQU4+LUaVwwG+e/wq7zR9SASXoOtYVB/b+D0FVXCEJoLtpDUxecEyv0viyFoU1fSXmO32uZ9uSOhq7wW6WWTCVn6kgJuSAmzeo4Kaar+CwYQG7oq5gztYi6aoNrbWHC14j/RoyULBS0tmRCI2tSFjQ/8Kl/QOjPdCRwvyHitBzEeUSzH03SVr2B7cE8pF8SbWV3yL+WjnJm1Sr1Up3DKBIdfzBWur0bIlqoClK+ITa3bVSqVbaDBpSCZll+862oe4orYP6P1Z+jhTguWOOgzCki4gjBcf6RXKhKsBR7+g==\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"e9d2Ej_yEdf2jCBDDJKVqsW-cXo\",\n" +
            "            \"x5t\": \"e9d2Ej_yEdf2jCBDDJKVqsW-cXo\",\n" +
            "            \"n\": \"1X8Pa5XWh6Ol3bLBQ5b6LN5KXe_R2W1w5IXbsNCRvabQf3Y9hgtR6ZUlSXotwrdmUzTkjpGys8rkZRp0C0FF19bMSrY8I1A6FJZXlwkeJ6VZDn76oW1eLBo4a-ec70UvhXm4sQnW3b2BWL0WLTWZng2MacDvNdq36CqmmNAPxuPs32JbtobWKivxzr5Jj_WTG6kxPJ30H-kRC1bUj7TrT464yiMDFljv-14AdDR2crEGGek9ZByW-zZaDxLwK8vyLvswn5B28S1infty1oqYUxP-NMNd9jXVIZoYhjjlhGAUs0vF5gMfFr4qUL7opOWF_hbwe9dCaTc6vN5yazMQGQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"oI2pysRK0KWZ9Jd0meQFJRHkyAE\",\n" +
            "            \"x5t\": \"oI2pysRK0KWZ9Jd0meQFJRHkyAE\",\n" +
            "            \"n\": \"ne1IUhdxcczsCYTOU3garyO2ltVnJRhl5fATBV1OGhEgEM4IneKZcpqzixnlLt-AJRqEwQH8u7sFPJWo1IlEpXK5auSLhNX3qbq7Fgzw-joWoTJwc5Gc46F03fi0pOnRzIkOrB2BXe6OyAVoQpd8sm8K7CPqD9Qkoc03i8dwG33tabHm0YS9Nw6aODPcl5F9A_YGo2KGo0-piOjX0kjoGgGnM6nbIDk5fVaXeboGHhFUwwyNd_Q8iRXmtxTAp7IZdk26beH4Zlc7NFHDqzQQKfKpGikcJKlwm0RR4y_IPaIsqQHlWZG8NMXknovaDgCZ6HuiiCq8gA22sT8pFHI5Cw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"AmtYnwhzv0SChDZtISzRyJ2Hvus\",\n" +
            "            \"x5t\": \"AmtYnwhzv0SChDZtISzRyJ2Hvus\",\n" +
            "            \"n\": \"0B0TUOPZUjs2D7soXEp9F-Ph8mycni1ufpx8HkY98MhGEL5CCRG1z_snTcEhwj9r2A6iLn7vWTn4l8KsR9E6WoPVwx3o2lsxESsJwFNe_ptAuVxnWKexa0UVZBsHrWjkS-I0IP0Qj92nDPZ_Hkeecp8wApRVojeO2JY1V92hgz22BUJ84G0IIlF0LiHEushTYMQzd2tBe3w4Wwg7rNrzIoAE7V2s-qe_snPxFHm0e4BKnTQrjjaDMtD8t1Quku2gVc5BFEiUDWd9DOjuFiFgkasYRrBJtoHwF9vzBUIsUIzQYAyKPGRrRPSuZzAelbVsORvpeVAvKh1fBfQpC6Qpnw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"MIIFCzCCA/OgAwIBAgITVgBVcd/xrkwi7dHIPwAAAFVx3zANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwpNU0lUIENBIFoyMB4XDTE4MDMyNzE2NDE0M1oXDTIwMDMxNjE2NDE0M1owJjEkMCIGA1UEAxMbY2hhdHNlcnZpY2UtcHJvZC1qd3RzaWduaW5nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0B0TUOPZUjs2D7soXEp9F+Ph8mycni1ufpx8HkY98MhGEL5CCRG1z/snTcEhwj9r2A6iLn7vWTn4l8KsR9E6WoPVwx3o2lsxESsJwFNe/ptAuVxnWKexa0UVZBsHrWjkS+I0IP0Qj92nDPZ/Hkeecp8wApRVojeO2JY1V92hgz22BUJ84G0IIlF0LiHEushTYMQzd2tBe3w4Wwg7rNrzIoAE7V2s+qe/snPxFHm0e4BKnTQrjjaDMtD8t1Quku2gVc5BFEiUDWd9DOjuFiFgkasYRrBJtoHwF9vzBUIsUIzQYAyKPGRrRPSuZzAelbVsORvpeVAvKh1fBfQpC6QpnwIDAQABo4ICQTCCAj0wJwYJKwYBBAGCNxUKBBowGDAKBggrBgEFBQcDATAKBggrBgEFBQcDAjA+BgkrBgEEAYI3FQcEMTAvBicrBgEEAYI3FQiH2oZ1g+7ZAYLJhRuBtZ5hhfTrYIFdhd7pa4HUhWcCAWQCARQwgYUGCCsGAQUFBwEBBHkwdzAxBggrBgEFBQcwAoYlaHR0cDovL2NvcnBwa2kvYWlhL01TSVQlMjBDQSUyMFoyLmNydDBCBggrBgEFBQcwAoY2aHR0cDovL3d3dy5taWNyb3NvZnQuY29tL3BraS9tc2NvcnAvTVNJVCUyMENBJTIwWjIuY3J0MB0GA1UdDgQWBBSYaznPwkaKip7K1vWraQCd7ncerzALBgNVHQ8EBAMCBaAwJgYDVR0RBB8wHYIbY2hhdHNlcnZpY2UtcHJvZC1qd3RzaWduaW5nMIG1BgNVHR8Ega0wgaowgaeggaSggaGGJWh0dHA6Ly9jb3JwcGtpL2NybC9NU0lUJTIwQ0ElMjBaMi5jcmyGPGh0dHA6Ly9tc2NybC5taWNyb3NvZnQuY29tL3BraS9tc2NvcnAvY3JsL01TSVQlMjBDQSUyMFoyLmNybIY6aHR0cDovL2NybC5taWNyb3NvZnQuY29tL3BraS9tc2NvcnAvY3JsL01TSVQlMjBDQSUyMFoyLmNybDAfBgNVHSMEGDAWgBRhy7uGYUFjMtVbZsaOt5xNAG8E+TAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDQYJKoZIhvcNAQELBQADggEBAIxS6khbi9XxA3Jidb1X52v5YlQW1HxnXQS08rpjivDQWELNnWLYHpIpgY99OME15KOZJ/uH4FDigiNEqwKp8xUjvvf4cl222sqfpI46LB66WbO6H5faCYhfYX18D6/Z3M179Mo+eLC4m0iQ4GadhC+zKv9zmuD44dIx2BEiVvlaVK1vwWTk5aMKnWmjd5TK+wmo1bcIKWeld4N9HCvW2eEGa4qeErtSAJ19qIGP1YVH+C3r6Xl7TxNQ9PWFLQthXqCPoKIclnbSk4oKtxANSKzIaX4X7X3FVw99TAw0ZrBvXDWe9SuK1A9VObkuvqX+VYYTj4A2unn1paLc4KqQgJ0=\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"b4ysOWItD13iQfLLeBFX9lRPDtk\",\n" +
            "            \"x5t\": \"b4ysOWItD13iQfLLeBFX9lRPDtk\",\n" +
            "            \"n\": \"yk4rv691yrqBWP5BsTfhLakl3a1txN26qePK1xxQh2H6Bdy8sGwiLfoeSPFAVPHZ7-c0rn0OsLG9EudWnMPJFk4k-ECA7R-17tsKelAIRWURX7Bhr8Qzo4jhOhkg-9i--5vjmnIub8k6DvJM4qq9TsGG3OrIIH2Q-eH7PyYqzcTvJjqPJripwbsnDx2eN-zwte2vTinuYfczN_I312rK-LtTEZjOHBct5RF6ri-y64nmsn7u2l_xGFIlPCaCsbxbI9X5reIs0lJJmeoUoh2qC7X0xxcpXiDP0ThMOTtEhxBTc--AWbL15cKqsF_gOUXZCIhG_zQEz2W3sOKSmrSd0Q\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"kAjFbciiCrTIdZ_LarP3k_Y28Tw\",\n" +
            "            \"x5t\": \"kAjFbciiCrTIdZ_LarP3k_Y28Tw\",\n" +
            "            \"n\": \"tM-8KFlvZ-coRu3fpwdevjlBKHKMOIrP7pozCkpL7gxaTfDjtLMtJBpSr8zMJmp5_0Q_zcaWsKQxxncalqM7gpThxLhYQMAqWTrj0xAJjJI40IMDBy4KtYcVSS-rQ2tLtjvNdGFufuT6igbtjmhzM7WmuQkLvePtDBwTPjMRSigT3unVuYD7-E7KsgI2LW0cVWTDRWtfLapI4_s16dRIDkphIAEe16exl2F-ZJhCrXbII_ZupPtdcPGQQXYUR3lJId2y4w1utVxzB7JtXpcWirSkg2QhV1hrLHdSxJsi97dEewQRPM0Unc4MV21cP4-c5pqVxLVHVhiEn_fluOYAMQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"QMrt9-4Jn43rldid1E-hByyRC_Q\",\n" +
            "            \"x5t\": \"QMrt9-4Jn43rldid1E-hByyRC_Q\",\n" +
            "            \"n\": \"wNVxPVj5ZE0py5uC0aztvhJWtiTVB5dfF_UW9FalDM8XF1o5wITe8p1ZmcI9pzJ8kxw3xT_8KtFzhCezHe6gEfr7GiunU9MEiXhaOYe_ugsfAIxfmrkHbnUJbkclaCdnFLXjH8YHJhLSedkN3vI_dcUjIyC74k7P1GMAEiPuuAfXHoBk1vxvZt33-Ub7nYAKHXudgGpPHjNlU5-bk9xnBRQQiVJxRpdmFm0i1kk7XsqPvZH4lIDxP36dhMw0N-cF4pPHBz7FIP8uaNYEmv8lbdM_aL5jsCd7kCuXMOMHLx5fR7hlGyKvo1wFWZPDQTsyI9LdRWxi0sJ_OJ_x8ffY6Q\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"MIIGtjCCBJ6gAwIBAgITIAABYVX1b0WS80q9QwAAAAFhVTANBgkqhkiG9w0BAQsFADCBizELMAkGA1UEBhMCVVMxEzARBgNVBAgTCldhc2hpbmd0b24xEDAOBgNVBAcTB1JlZG1vbmQxHjAcBgNVBAoTFU1pY3Jvc29mdCBDb3Jwb3JhdGlvbjEVMBMGA1UECxMMTWljcm9zb2Z0IElUMR4wHAYDVQQDExVNaWNyb3NvZnQgSVQgVExTIENBIDIwHhcNMTcxMTIxMjMzNjMxWhcNMTgxMTIxMjMzNjMxWjAdMRswGQYDVQQDDBIqLmJvdGZyYW1ld29yay5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDA1XE9WPlkTSnLm4LRrO2+Ela2JNUHl18X9Rb0VqUMzxcXWjnAhN7ynVmZwj2nMnyTHDfFP/wq0XOEJ7Md7qAR+vsaK6dT0wSJeFo5h7+6Cx8AjF+auQdudQluRyVoJ2cUteMfxgcmEtJ52Q3e8j91xSMjILviTs/UYwASI+64B9cegGTW/G9m3ff5RvudgAode52Aak8eM2VTn5uT3GcFFBCJUnFGl2YWbSLWSTteyo+9kfiUgPE/fp2EzDQ35wXik8cHPsUg/y5o1gSa/yVt0z9ovmOwJ3uQK5cw4wcvHl9HuGUbIq+jXAVZk8NBOzIj0t1FbGLSwn84n/Hx99jpAgMBAAGjggJ+MIICejALBgNVHQ8EBAMCBLAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMBMB0GA1UdEQQWMBSCEiouYm90ZnJhbWV3b3JrLmNvbTAdBgNVHQ4EFgQUZQGzJ4WsAcOm1d1r25L7K6CvHJcwHwYDVR0jBBgwFoAUkZ47RGw9V5xCdyo010/RzEqXLNowgawGA1UdHwSBpDCBoTCBnqCBm6CBmIZLaHR0cDovL21zY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvTWljcm9zb2Z0JTIwSVQlMjBUTFMlMjBDQSUyMDIuY3JshklodHRwOi8vY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvTWljcm9zb2Z0JTIwSVQlMjBUTFMlMjBDQSUyMDIuY3JsMIGFBggrBgEFBQcBAQR5MHcwUQYIKwYBBQUHMAKGRWh0dHA6Ly93d3cubWljcm9zb2Z0LmNvbS9wa2kvbXNjb3JwL01pY3Jvc29mdCUyMElUJTIwVExTJTIwQ0ElMjAyLmNydDAiBggrBgEFBQcwAYYWaHR0cDovL29jc3AubXNvY3NwLmNvbTA+BgkrBgEEAYI3FQcEMTAvBicrBgEEAYI3FQiH2oZ1g+7ZAYLJhRuBtZ5hhfTrYIFdhNLfQoLnk3oCAWQCARYwTQYDVR0gBEYwRDBCBgkrBgEEAYI3KgEwNTAzBggrBgEFBQcCARYnaHR0cDovL3d3dy5taWNyb3NvZnQuY29tL3BraS9tc2NvcnAvY3BzMCcGCSsGAQQBgjcVCgQaMBgwCgYIKwYBBQUHAwIwCgYIKwYBBQUHAwEwDQYJKoZIhvcNAQELBQADggIBAHSW3o+Fj8StjFURPZAwqrrR/KYVVc0OrKPP3o/SIXN/XkH/O/I7iGgnwNpyHxKPNLBmYVqbcyRwn9P/UONbYMH7gGCi58P2i8s4T53+qDHbvmTcecDEEpNDL7pp8OeyUCcT0N6QQJz+mVyf8zAH0j2ZuUpSrNuLzUSTSgY2Pv9F3lme3N3NlaOnRSkZn3slhlMWXXx6Clc7StbwVFlavzt/9YA1GQXLXJn73rybKWnQo41o8dTqwwHhKZGHoB6flKCvXiusS1glje2ff16BA6ZoEnpBFVkg8DNQo7kar4eB/AfTlhl/oqxBDvB27qwfsOTo7QSioWzCqdjrjJNZ+nKfWcqrQtTwDROQGUga8ezm5k6/YKU4lrTOQ3HSCfRHJHjOOUGk+obdzC3yDenAbqmjElT8G6Ks23LM7RysvDwvI7D+AKbiONbLGmoGT/izZDYd7o+8Xkwvx5chtskRpG5f4MD96jwlgq38IrZzslkvLqmdUEEDtKkH28pUZ32oeh7DNTNSi1Ox8wyah906Yjs8ZW4FqmjdZewPlU1m8UMTo2c/rmbommScZ21fuOgXltrVZuI4XnLtYng4uGfd6oOFsdoz/s5z7LIEzrd3EH/fMz0gdidADIDfQP6PH2xKrn/H5jUrpJzfqVfbohrUHwDDMbajONDRskUReeOxQui1\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"GCxArXoN8Sqo7PweA7-z65denJQ\",\n" +
            "            \"x5t\": \"GCxArXoN8Sqo7PweA7-z65denJQ\",\n" +
            "            \"n\": \"uknv8y-QkKiiyc_MecJ1o4JE_5Fz94m1_y4r9-pKmyIMZI3Lx3G-pyWQzjS0UkpVB0-ujRKrvlMXPJT0npQM1hhbkHZThmHarcXXk84nqq7ZTZavwWVXjFZTh3CAY5cU65mTivc8j2Sn3u6fP_Mp4E_kyTko0PDPvRh6pum4c1_DSxC5Rg53iJ1C3r4aaTznZ0yCRw8u-AvV_5JCbPDoCH0aFAWWVB2xN9KHmkAy16ucT3KRhu74IQVATuSGSK59aqp4ZlcHgu_tGqEpt_uN2Qm0W6c8gI-2V6JTCTql8uet1jS6FuuqXzx325LTSslRJ3CjJ7uP0LOgZbnhBI96xQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"MIIGNTCCBB2gAwIBAgITWgADkA5U98sxQsuZyAABAAOQDjANBgkqhkiG9w0BAQsFADCBizELMAkGA1UEBhMCVVMxEzARBgNVBAgTCldhc2hpbmd0b24xEDAOBgNVBAcTB1JlZG1vbmQxHjAcBgNVBAoTFU1pY3Jvc29mdCBDb3Jwb3JhdGlvbjEVMBMGA1UECxMMTWljcm9zb2Z0IElUMR4wHAYDVQQDExVNaWNyb3NvZnQgSVQgU1NMIFNIQTIwHhcNMTYwNjA4MTczMTI0WhcNMTgwMzA4MTczMTI0WjAfMR0wGwYDVQQDExRhcGkuYm90ZnJhbWV3b3JrLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALpJ7/MvkJCoosnPzHnCdaOCRP+Rc/eJtf8uK/fqSpsiDGSNy8dxvqclkM40tFJKVQdPro0Sq75TFzyU9J6UDNYYW5B2U4Zh2q3F15POJ6qu2U2Wr8FlV4xWU4dwgGOXFOuZk4r3PI9kp97unz/zKeBP5Mk5KNDwz70YeqbpuHNfw0sQuUYOd4idQt6+Gmk852dMgkcPLvgL1f+SQmzw6Ah9GhQFllQdsTfSh5pAMternE9ykYbu+CEFQE7khkiufWqqeGZXB4Lv7RqhKbf7jdkJtFunPICPtleiUwk6pfLnrdY0uhbrql88d9uS00rJUSdwoye7j9CzoGW54QSPesUCAwEAAaOCAfswggH3MB0GA1UdDgQWBBTjF3akDGa3FS102tkwztuQuhKHlzALBgNVHQ8EBAMCBLAwHwYDVR0jBBgwFoAUUa8kJpz0aCJXgCYrO0ZiFXsezKUwfQYDVR0fBHYwdDByoHCgboY2aHR0cDovL21zY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvbXNpdHd3dzIuY3JshjRodHRwOi8vY3JsLm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcmwvbXNpdHd3dzIuY3JsMHAGCCsGAQUFBwEBBGQwYjA8BggrBgEFBQcwAoYwaHR0cDovL3d3dy5taWNyb3NvZnQuY29tL3BraS9tc2NvcnAvbXNpdHd3dzIuY3J0MCIGCCsGAQUFBzABhhZodHRwOi8vb2NzcC5tc29jc3AuY29tMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBOBgNVHSAERzBFMEMGCSsGAQQBgjcqATA2MDQGCCsGAQUFBwIBFihodHRwOi8vd3d3Lm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9jcHMAMCcGCSsGAQQBgjcVCgQaMBgwCgYIKwYBBQUHAwEwCgYIKwYBBQUHAwIwHwYDVR0RBBgwFoIUYXBpLmJvdGZyYW1ld29yay5jb20wDQYJKoZIhvcNAQELBQADggIBALSt1pveCkmT9Zj5diS1I3PXtlqqwOWzLFO84WhaoiT+7nfDlevjEd7eiyNaJoLsC+d5DLMySk9/rLPSZrdQgxDa+/0p9EoKXr1elgojdKlMZzeax6ymD9S/cN6fJ5Su9uzTfAz3+x617g8K3EDfkDoNniDzi36NL9blvDhDWLdNaO7gyvA1TU5wwjNR8QIbVhsG3EruT7WGGBsTw+BWdbq4VM2iFSsplNokvuUlCSwljWbHHbOjWVlfmTtfW/9ml8rCZ/YoORrVtC0QEEkKYnOnGiqxNIpKoBv9YrMCF0b98SfNZOqd7PDrbNmwl7Dg6rY47QF40UFYx/KhuAESsBzpfeDQuhHb/Y53hveJrL5i+VYHpD8jhqsXIBO9tHMWRX4zrhMZ06HE4Pr+0lW1sZnasD0bjnz3rmVswcwfbTLIe6doEfO+i3yoQ9ktwH1ZbWngB9WOgJoPvl7m7V5tiKxok3BkRY6I4vNdhBcyf2fjOEFRYUkmuPbP5UrICGziwqzKjy2aOQ31DJ3iWDBl/JfswxpsE2yBRE0QoIHSeQKlFYJa5RgLi15OnQY5mUZvI8uhVFUqncdmGBzII4KLL/KYCqhLDb1+itFYvR022RDRRdeQfwpFHnt8T0S+L6ViAYEaE211yr71wGLknX+wQZI4N0aJ1iGvXc1ehHLKbDvb\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"bing\",\n" +
            "                \"directline\",\n" +
            "                \"email\",\n" +
            "                \"facebook\",\n" +
            "                \"groupme\",\n" +
            "                \"kik\",\n" +
            "                \"skype\",\n" +
            "                \"slack\",\n" +
            "                \"sms\",\n" +
            "                \"telegram\",\n" +
            "                \"webchat\",\n" +
            "                \"test\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"M52DDXzhCd9LEhK6P6xszOFwApU\",\n" +
            "            \"x5t\": \"M52DDXzhCd9LEhK6P6xszOFwApU\",\n" +
            "            \"n\": \"rF9YkEwvrezviiq2i9akoxR5TYwJU8EkLdFNOCX1XRjDd9czhIj3hK-aQT9D7bJDnOPlINbMKzM8OZr3TdKuTywO3pB40CfAlDuk7sNITntQR_yaO0T226Bo8q5rhbcnzue4p-89xPdybIudBs1fQdYkbYV4eEQ8Q4qMUKNa1x1sO7PPNcxwsHpk0dQekbDKYpiwyxd1KC_T3ZvE8g7fPiG2KPmxjfx5kUuBL0MjD4qq0bcPU9oDfcBKErnW0t9nz71PgDa4ec8PP0F81wTYGWX_qCJf3txvXauQoVv8oJ_ZhNSYkVL9448ochf15_HXa2oCTHtE_dWH4oYfsGbYNQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"kaizala\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"6TTY9b3l-nW7xxYStL1a0Em5GWI\",\n" +
            "            \"x5t\": \"6TTY9b3l-nW7xxYStL1a0Em5GWI\",\n" +
            "            \"n\": \"8mB09E1Q-auiiEMxj5RrJqtesYhXF34GbamBUVrq6EGba29b2P8Gsp5f3bzNbKBado6zI4XDCCWgLWaBM67_XyVZUfkLhxJ-60R2NOvdERdZSrn4zRYcKqATJGJeK2J5EEVXA7kMPXDdNBSaxnlju90BjmQSfbop4Yoy2Noh8yoX_125WvWE13z0oZ6d0tM0DQv0BX3oYJVBEPJE_Rber5kIeov7F3cyXAmabKfePGADMu7PEwghb27R4UcgF8udGlM4c-Fa-jYPY5careEwIX48FDqqTXGSRpERWQSyMw0ocKl0g7b9nFHUNcDyN31bQ87p3THVB5gN3sIPGhulcQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"mswallet\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"4yigbG7UW7-dKjzjm7CBHkBCK6k\",\n" +
            "            \"x5t\": \"4yigbG7UW7-dKjzjm7CBHkBCK6k\",\n" +
            "            \"n\": \"ykQFXaeSBlvdvfXoTMnjiVoegOcKZQwVPSGviWJF2qmTrxyu6pTnJQokz-JuFcvF7RohPyCczbZnH9SVHVmnIY5WtmKfDH1Pnp02_a_nntpyH-gBbaBt7SsMs5hEuJY0dQl4ikU6k6TMftAkgzTE7DMLxfhHULv7O9okokcGBlL2-HsN_nOomR7Vm43J4KGzW9bLCFGnrooNwpW8mAl4drgduNK8VMms97Vq7eNmCGl6D1wQLP3DAaeX4Tv0T4w3jn7n9HF1DYHdND5iSCI_3EZCse9rkkrElnL3hh1ujo0iQESG3FV8-sD6QaLKc-upTO29Dn_Fpftn0sXMv4UPFw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"cortana\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"YAJk7OgPGmt_qOu7DHj_E9cTb7s\",\n" +
            "            \"x5t\": \"YAJk7OgPGmt_qOu7DHj_E9cTb7s\",\n" +
            "            \"n\": \"3Ky8bu_G7hwuRITp3B5NsqBp4YR-yHDRRjODh4pOCirRZw5-Mu4aPCQRXXnfoL_mefgILKaEjYW6MDDlXl-3gOCZjUeUis3UbR_3XRusWzhtDwkrY3gFO6oH5h6FXLVQMGWc26nq4JwETQ6avnX_IjGbjt098V1SDHkjSHw48VRyP8ZHFE6cqnXryuE93jEpHOn1ImrBkwpmwElryvAU8Q5G_O_pDoSZ2eIN5jIZO5Eeo0ybwJSfcS6SjDE0FcWNGOJhTmK1PvtLHNWGPiRR3i1YNeCpiH0524_5r3FRS0RO2gn51j8lSreNpDk9xuG0ohLmI7z_dboHr_J3QVe3Zw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"cortana\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"jcZZmJ_oTfzxRl279RmddxC2rJI\",\n" +
            "            \"x5t\": \"jcZZmJ_oTfzxRl279RmddxC2rJI\",\n" +
            "            \"n\": \"w_KlTQ0psQTpNt_SHuGnZGV28apDbeeAxbAkMbpB8-mymAGZhmQChcGFxrGFRjtyryM0Y4AERDSh7jAeEaySoAIPwg0kYuXVLBkF-9nTX0WenMbtQuOs3h8I7L_SMqehAZydmQVPDFf57RxsxPrt-q2YGRnEXIp34uEQ3JpA6AK5xkpOZajOEj1W5oEGLu1NyekIojBJ0axdZcbd526TnM_NRQpvrCAd-28bkinQ6a7UDZISFgXPhp97YQvmzqZPN5kVywds8b3yt7p6qJDr4Tmxd-eETupKITpihiZcrH7sv1SmT0kjDP98SGpyiZpG7vcNNYQTbrJKHnA1__KEUQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"mswallet\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"WyM5Su8S01KUBTIgif5sIiDT_L8\",\n" +
            "            \"x5t\": \"WyM5Su8S01KUBTIgif5sIiDT_L8\",\n" +
            "            \"n\": \"9undo92bGpW2vDOKexKBcD1N4KJslXqTwbSKsSSgQC-I-t84GqmIN0zHJK08d-dVKTDy5WuOAGO9Giz6O0_vV-DqqjeRoVwwkr_TY5X2GhoYwxY7vvtOjzQVKqYDbB4iGj9qLKDjr7mYLJRiDR9xaHxTrbMNv4oF7sXv5JWXp9ETOtO2aidEPJZjXLUKL8XDfkSzrYDj4VYNGpL5Vo-7dytwqw_KWT-6M6edgqsFOaxGXQbCslR3bpolo6d4Wf2CgKaUiBqEMPYn_rngYlopsfP-5jdadVtjYHB4cy2V2oAIKx58t5sf5cbQ56jAziqcpVlbPXMgMwyqnHAK7crEQw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skypeforbusiness\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"MPL7UeVQQ_goYAnmVtQKBeh9jyc\",\n" +
            "            \"x5t\": \"MPL7UeVQQ_goYAnmVtQKBeh9jyc\",\n" +
            "            \"n\": \"xNDianGGQb1mGbgHryefUdi25zW6xkNp8JbdtZaWXu2FqYXvW0AJ63QChplZ1uSnRnbpakFGdRNqjs5Xx9eJy5iHHvCtu5YEMIVFnRWs8pfX89OrYl_4o5TvJCTCSd_SIPjpcVOn4qT0kgVxR46G9AT0CRYAsN-cOrk-Csc8shAmdifCZseAkrhzfzH5V6f_tTx34yilSnOYwpnq2zKhUQanUsqxnyxSoIp8tLKItC4M2rxM7K1ZQblUU7zrw8I79X9ggipvmAnysqxg1E0Xe6HhLYH7PGytfGujCJK3HbT1R-2flANymxmLw6NM6W34Skqo5kUXKD7vbrRuaIcMZQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skypeforbusiness\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"Yy8o8Zu8mNpbAOvtWIe5sJbqfZQ\",\n" +
            "            \"x5t\": \"Yy8o8Zu8mNpbAOvtWIe5sJbqfZQ\",\n" +
            "            \"n\": \"uf7n4oYEu1rUjtTHzcYksBM8fuNJK01mkg5WvXlJGLQxhCBfFNIdUa92xfagiPuhO0QQqRnGjcJFz9zj-kuo7oWLWeOWUJLS794LkKDaFDdjnxm5Wz_3-ET-evkxmsDbXDbBwKFjrFzyk7nJ0zI9iYtE0j6M8XVHzlVYoo0Pjd3ik5PkQCXkMfXshb0OTzEBAUlPmoiVPQYSEOWLFpSz1lcR8G-qHUpADe31_RSIY2dCLojM4xHsD6f7DOQOkzroyIBuVGsWwEfbRdRlaMXgPmsUx7xtgYbZw1loaLMVUY5qaVCgV2f06OYzDAtby7DEo6bcyVxsm7K6-uTbjchusQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"MIIE/TCCA+WgAwIBAgITVgBPpV5OJTKG2Dr57QAAAE+lXjANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwpNU0lUIENBIFoyMB4XDTE4MDIwNzIyMjUzOVoXDTIwMDEyODIyMjUzOVowHzEdMBsGA1UEAxMUc21iYS1nY2Mtand0LXNpZ25pbmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5/ufihgS7WtSO1MfNxiSwEzx+40krTWaSDla9eUkYtDGEIF8U0h1Rr3bF9qCI+6E7RBCpGcaNwkXP3OP6S6juhYtZ45ZQktLv3guQoNoUN2OfGblbP/f4RP56+TGawNtcNsHAoWOsXPKTucnTMj2Ji0TSPozxdUfOVViijQ+N3eKTk+RAJeQx9eyFvQ5PMQEBSU+aiJU9BhIQ5YsWlLPWVxHwb6odSkAN7fX9FIhjZ0IuiMzjEewPp/sM5A6TOujIgG5UaxbAR9tF1GVoxeA+axTHvG2BhtnDWWhosxVRjmppUKBXZ/To5jMMC1vLsMSjptzJXGybsrr65NuNyG6xAgMBAAGjggI6MIICNjAnBgkrBgEEAYI3FQoEGjAYMAoGCCsGAQUFBwMBMAoGCCsGAQUFBwMCMD4GCSsGAQQBgjcVBwQxMC8GJysGAQQBgjcVCIfahnWD7tkBgsmFG4G1nmGF9OtggV2F3ulrgdSFZwIBZAIBFDCBhQYIKwYBBQUHAQEEeTB3MDEGCCsGAQUFBzAChiVodHRwOi8vY29ycHBraS9haWEvTVNJVCUyMENBJTIwWjIuY3J0MEIGCCsGAQUFBzAChjZodHRwOi8vd3d3Lm1pY3Jvc29mdC5jb20vcGtpL21zY29ycC9NU0lUJTIwQ0ElMjBaMi5jcnQwHQYDVR0OBBYEFCt2Z50SdKRq9oS7B5/dC9Wkd4h2MAsGA1UdDwQEAwIFoDAfBgNVHREEGDAWghRzbWJhLWdjYy1qd3Qtc2lnbmluZzCBtQYDVR0fBIGtMIGqMIGnoIGkoIGhhiVodHRwOi8vY29ycHBraS9jcmwvTVNJVCUyMENBJTIwWjIuY3JshjxodHRwOi8vbXNjcmwubWljcm9zb2Z0LmNvbS9wa2kvbXNjb3JwL2NybC9NU0lUJTIwQ0ElMjBaMi5jcmyGOmh0dHA6Ly9jcmwubWljcm9zb2Z0LmNvbS9wa2kvbXNjb3JwL2NybC9NU0lUJTIwQ0ElMjBaMi5jcmwwHwYDVR0jBBgwFoAUYcu7hmFBYzLVW2bGjrecTQBvBPkwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMA0GCSqGSIb3DQEBCwUAA4IBAQCDDeI/a8x0vxbTiAQUH6lqacP+wFxGNs1/C3KV5H5LJak4NzYth6kHlwvQel8Q2mG7iDwTI70MnYVGLmvVtxyx5U13GbsD/kJegR+SXFJ8U+gJRdZr9VlUoFRc0/BGui81eRUnQFIkW5mSViictXFL+G41Ra7aUnKN/mbvTHkedsvnqxiIvCaAw7CAVRqDFN50N9SrYe4Wqh4UGXVWD5n7fZdhv6GNhBz5HlVM2TOrTqb/IbgTYyj6uQDQ3p6JqKHK0bZOmUs4Ih17Cm4FOSaqHsrt+g6FCf680HS6VeJIBxnFbJy12yOt7B7pm2MzYmJNeH31VBLr8HN1TJ390fyt\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"yceHp93RzmN-kd8vCnKEulqI8Zo\",\n" +
            "            \"x5t\": \"yceHp93RzmN-kd8vCnKEulqI8Zo\",\n" +
            "            \"n\": \"uWfitwq1Pr_shNl3_qmR9c4ue6TM_uc-alWs6aY1Hif7HGMdQQGLWbh-o4WDO2bXhFJPXxB0eRm-GXrHqfBB1Ne5iWiTldOFYTbLXSEiILrbAi9A2rHdAuxuWIeQDDHPOb6tII_jvHNXA24q5paXktaSV0arn4fRpOMmi_eW4QQqPK0LeTNfd4LhGpKX26o8Fc3hu9RSUUbVXo1qMYyP3U75PBWSqYVGIXnFRu9FbLP8AmSCzZB0RI5kQEZQ-zs70cCnyBp1bGZI6J_qDwFlyQmH7Y-0kCfxONAItLe7vRNnCn1104EpObqg22oFKeYuwXD2jlZILfBifl_ETQPyvw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"skype\",\n" +
            "                \"msteams\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"use\": \"sig\",\n" +
            "            \"kid\": \"E58u3c-zKB_0zQrLd9h6GyVr-ik\",\n" +
            "            \"x5t\": \"E58u3c-zKB_0zQrLd9h6GyVr-ik\",\n" +
            "            \"n\": \"r8lhF26IdzLOnBbZB_NSLcU2KaozloX4yKIgtUXhBCdAaHh85VrscT21jCy8laSipAxt1hswO4bwNEOkTfwDooMHn_CRyfk6Xcp83lYwy5AWQjuP6aK84aJNg8sOkVY7yJB0vy121BW4spi-f42MtBNEeYfxE-Yv7BdfFwYT8GglEQRXQ9F-zcedkNPmX18lxE15q8JX0L-o50NmxMUIsS4XN7Ipp9wtgExx9HZlObFuepyojPI4TfTyXN_bg-vjUSzjQBQVcD0ISoz36R-kmFv_pQb2DbDhgM-qgQAePBjPRy0ELuHzvGjJZ1VmHIiS1SSGPCiaq7lP5rQpBhgJjw\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"x5c\": [\n" +
            "                \"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\"\n" +
            "            ],\n" +
            "            \"endorsements\": [\n" +
            "                \"wechat\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}"

    lateinit var parsedResponse : SigningKeysWrapper
    @Before
    fun setUp() {
        val objectMapper = ObjectMapper()
        parsedResponse = objectMapper.readValue(response, SigningKeysWrapper::class.java)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun testMyApplication() {
        val compactJws = validToken.substring("Bearer ".length)

        var signedJWT: SignedJWT? = null

        try {
            signedJWT = SignedJWT.parse(compactJws)
        } catch (e:Throwable) {

        }
        val signedKey = parsedResponse.keys.find { it.kid == signedJWT!!.header.keyID}!!

        val cert = X509CertChainUtils.parse(signedKey.x5c.map { Base64(it) })
        val rsaJWK = RSAKey.parse(cert[0])
        val verifier : JWSVerifier = RSASSAVerifier(rsaJWK)
        signedJWT!!.verify(verifier)
    }

}