package com.echeo.app.data

import org.json.JSONArray
import org.json.JSONObject

data class Snapshot(
    val types: List<SubscriptionTypeEntity>,
    val clients: List<ClientEntity>,
    val subs: List<SubscriptionEntity>,
    val renewals: List<RenewalEntity>,
)

/** Sauvegarde au format JSON versionné : un seul fichier, facile à garder ou à envoyer. */
object BackupCodec {
    private const val VERSION = 1

    fun encode(s: Snapshot): String {
        val root = JSONObject()
            .put("app", "echeo")
            .put("version", VERSION)
            .put(
                "types",
                JSONArray(
                    s.types.map { t ->
                        JSONObject()
                            .put("id", t.id)
                            .put("name", t.name)
                            .put("durations", t.durations)
                            .put("price", t.price ?: JSONObject.NULL)
                            .put("icon", t.icon)
                            .put("color", t.color)
                            .put("enabled", t.enabled)
                            .put("is_custom", t.isCustom)
                            .put("sort_order", t.sortOrder)
                    },
                ),
            )
            .put(
                "clients",
                JSONArray(
                    s.clients.map { c ->
                        JSONObject()
                            .put("id", c.id)
                            .put("name", c.name)
                            .put("phone", c.phone)
                            .put("created_at", c.createdAt)
                    },
                ),
            )
            .put(
                "subscriptions",
                JSONArray(
                    s.subs.map { x ->
                        JSONObject()
                            .put("id", x.id)
                            .put("client_id", x.clientId)
                            .put("type_id", x.typeId)
                            .put("duration_months", x.durationMonths)
                            .put("start_day", x.startDay)
                            .put("expiry_day", x.expiryDay)
                            .put("last_contact_at", x.lastContactAt ?: JSONObject.NULL)
                    },
                ),
            )
            .put(
                "renewals",
                JSONArray(
                    s.renewals.map { r ->
                        JSONObject()
                            .put("id", r.id)
                            .put("subscription_id", r.subscriptionId)
                            .put("renewed_at", r.renewedAt)
                            .put("previous_expiry_day", r.previousExpiryDay)
                            .put("new_expiry_day", r.newExpiryDay)
                            .put("amount", r.amount ?: JSONObject.NULL)
                    },
                ),
            )
        return root.toString(2)
    }

    /** Lit une sauvegarde. Lève une exception si le fichier n'est pas une sauvegarde Echeo. */
    fun decode(text: String): Snapshot {
        val root = JSONObject(text)
        require(root.optString("app") == "echeo") { "Ce fichier n'est pas une sauvegarde Echeo." }
        return Snapshot(
            types = root.getJSONArray("types").objects().map { o ->
                SubscriptionTypeEntity(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    durations = o.getString("durations"),
                    price = if (o.isNull("price")) null else o.getDouble("price"),
                    icon = o.getString("icon"),
                    color = o.getLong("color"),
                    enabled = o.getBoolean("enabled"),
                    isCustom = o.getBoolean("is_custom"),
                    sortOrder = o.getInt("sort_order"),
                )
            },
            clients = root.getJSONArray("clients").objects().map { o ->
                ClientEntity(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    phone = o.getString("phone"),
                    createdAt = o.getLong("created_at"),
                )
            },
            subs = root.getJSONArray("subscriptions").objects().map { o ->
                SubscriptionEntity(
                    id = o.getLong("id"),
                    clientId = o.getLong("client_id"),
                    typeId = o.getLong("type_id"),
                    durationMonths = o.getInt("duration_months"),
                    startDay = o.getLong("start_day"),
                    expiryDay = o.getLong("expiry_day"),
                    lastContactAt = if (o.isNull("last_contact_at")) null else o.getLong("last_contact_at"),
                )
            },
            renewals = root.getJSONArray("renewals").objects().map { o ->
                RenewalEntity(
                    id = o.getLong("id"),
                    subscriptionId = o.getLong("subscription_id"),
                    renewedAt = o.getLong("renewed_at"),
                    previousExpiryDay = o.getLong("previous_expiry_day"),
                    newExpiryDay = o.getLong("new_expiry_day"),
                    amount = if (o.isNull("amount")) null else o.getDouble("amount"),
                )
            },
        )
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
