package com.echeo.app.ui.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echeo.app.R
import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.abonnementsLabel
import com.echeo.app.domain.clientsLabel
import com.echeo.app.ui.components.ClientRow
import com.echeo.app.ui.components.EcheoCard
import com.echeo.app.ui.components.EcheoHeader
import com.echeo.app.ui.components.EmptyIllustration
import com.echeo.app.ui.components.HeaderTitle
import com.echeo.app.ui.components.PrimaryButton
import com.echeo.app.ui.components.RowDivider
import com.echeo.app.ui.theme.EcheoColors

enum class ClientFilter(val label: String, val status: SubStatus?) {
    All("Tous", null),
    Active("Actifs", SubStatus.ACTIVE),
    Soon("Bientôt", SubStatus.SOON),
    Expired("Expirés", SubStatus.EXPIRED),
}

/** Garde les clients qui correspondent à la recherche (nom ou téléphone) et au filtre. */
fun filterClients(clients: List<ClientSummary>, query: String, filter: ClientFilter): List<ClientSummary> {
    val q = query.trim()
    val qDigits = q.filter { it.isDigit() }
    return clients.filter { c ->
        val matchesFilter = filter.status == null || c.status == filter.status
        val matchesQuery = q.isEmpty() ||
            c.name.contains(q, ignoreCase = true) ||
            (qDigits.isNotEmpty() && c.phone.filter { it.isDigit() }.contains(qDigits))
        matchesFilter && matchesQuery
    }
}

/** Liste des clients : recherche par nom ou téléphone, filtres par état, bouton Ajouter. */
@Composable
fun ClientsContent(
    clients: List<ClientSummary>,
    subscriptionCount: Int,
    query: String,
    filter: ClientFilter,
    onQueryChange: (String) -> Unit,
    onFilterChange: (ClientFilter) -> Unit,
    onAddClient: () -> Unit,
    onOpenClient: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = clients.isEmpty()

    Column(modifier.fillMaxSize()) {
        EcheoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    HeaderTitle(
                        "Clients",
                        if (isEmpty) clientsLabel(0) else "${clientsLabel(clients.size)} · ${abonnementsLabel(subscriptionCount)}",
                    )
                }
                if (!isEmpty) PrimaryButton("Ajouter", onAddClient)
            }
            if (!isEmpty) {
                Spacer(Modifier.height(16.dp))
                SearchField(query, onQueryChange)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClientFilter.entries.forEach { f ->
                        FilterChip(f.label, selected = f == filter) { onFilterChange(f) }
                    }
                }
            }
        }

        if (isEmpty) {
            EmptyClients(onAddClient, Modifier.weight(1f))
        } else {
            val visible = filterClients(clients, query, filter)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
            ) {
                item {
                    if (visible.isEmpty()) {
                        Text(
                            "Aucun client ne correspond.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        )
                    } else {
                        EcheoCard(Modifier.fillMaxWidth()) {
                            visible.forEachIndexed { index, c ->
                                if (index > 0) RowDivider()
                                ClientRow(
                                    initials = c.initials,
                                    title = c.name,
                                    subtitle = "${c.phone} · ${abonnementsLabel(c.subscriptionCount)}",
                                    status = c.status,
                                    coloredAvatar = false,
                                    onClick = { onOpenClient(c.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EcheoColors.SurBleu)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = EcheoColors.TexteSurBleu,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Nom ou téléphone", style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSurBleu, fontSize = 16.sp))
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(EcheoColors.Turquoise),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) EcheoColors.Turquoise else EcheoColors.SurBleu)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            ),
        )
    }
}

/** État vide : invitation à créer le premier client. */
@Composable
private fun EmptyClients(onAddClient: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyIllustration(R.drawable.ic_user_plus)
        Spacer(Modifier.height(16.dp))
        Text("Ajoutez votre premier client", style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp), textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Ses abonnements et ses dates d'expiration apparaîtront ici.",
            style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Ajouter un client", onAddClient, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp))
    }
}
