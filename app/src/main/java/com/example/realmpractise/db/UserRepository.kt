package com.example.realmpractise.db

import io.realm.Realm
import io.realm.RealmResults

interface UserRepository {
    fun addUser(realm: Realm, user: User): Boolean
    fun getUser(realm: Realm, name: String): RealmResults<User>
    fun getAllUsers(realm: Realm): RealmResults<User>
    fun deleteUser(realm: Realm, id: Int)
}

class UserModule : UserRepository {

    override fun addUser(realm: Realm, user: User): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(user)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun deleteUser(realm: Realm, id: Int) {
        realm.executeTransaction { r ->
            r.where(User::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    override fun getUser(realm: Realm, name: String): RealmResults<User> =
        realm.where(User::class.java).contains("name", name).findAll()


    override fun getAllUsers(realm: Realm): RealmResults<User> =
        realm.where<User>(User::class.java).findAll()

}