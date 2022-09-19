package com.mergenc.barista.network.realtimedatabase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class PaymentMethod(val selectedPaymentMethod: String? = "")