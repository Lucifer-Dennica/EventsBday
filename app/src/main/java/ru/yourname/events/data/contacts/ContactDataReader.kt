package ru.yourname.events.data.contacts

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract

object ContactDataReader {

    fun readPhone(resolver: ContentResolver, contactUri: Uri): String? {
        val contactId = resolveContactId(resolver, contactUri) ?: return null
        return resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }

    fun readEmail(resolver: ContentResolver, contactUri: Uri): String? {
        val contactId = resolveContactId(resolver, contactUri) ?: return null
        return resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }

    private fun resolveContactId(resolver: ContentResolver, contactUri: Uri): Long? {
        val uri = ContactsContract.Contacts.lookupContact(resolver, contactUri) ?: return null
        return resolver.query(uri, arrayOf(ContactsContract.Contacts._ID), null, null, null)
            ?.use { c ->
                if (c.moveToFirst()) {
                    c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                } else null
            }
    }
}
