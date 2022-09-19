package com.mergenc.barista.network.analytics

object AnalyticsEventConstants {
    object CUSTOMER {
        const val QR_CODE_GENERATED = "qr_code_generated"
        const val QR_CODE_ID = "qr_code_id"
    }

    object CASHIER {
        object QR_CODE_SCANNING {
            const val QR_CODE_SCANNED = "qr_code_scanned"
            const val QR_CODE_VALUE = "qr_code_value"
        }

        object PRICE_DIALOG {
            const val PRICE_DIALOG_CONFIRM = "price_dialog_confirm"

            // Price
            const val PRICE_DIALOG_PRICE = "price_dialog_price"
        }
    }
}