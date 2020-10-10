package com.example.realmpractise.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User : RealmObject() {
    @PrimaryKey
    var id: String? = null
    var pp: String? = null
    var name: String? = null
    var phone: String? = null
}