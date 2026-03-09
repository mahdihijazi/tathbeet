package com.quran.tathbeet.ui.features.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.BodyTextCard
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.InfoActionCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.model.AccountMode
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.SyncState
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.displayLabelRes

@Composable
fun SharedProfileScreen(
    profile: AppProfile,
    accountMode: AccountMode,
    syncState: SyncState,
    onGuardianToggled: (Guardian) -> Unit,
    onSimulateSync: () -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.shared_title),
        subtitle = stringResource(R.string.shared_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.shared_eyebrow),
                title = profile.name.asString(),
                body = stringResource(R.string.shared_body),
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.shared_editors_title),
                subtitle = stringResource(R.string.shared_editors_subtitle),
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Guardian.entries.forEach { guardian ->
                    FilterChip(
                        selected = guardian in profile.guardians,
                        onClick = { onGuardianToggled(guardian) },
                        label = {
                            Text(
                                if (guardian == Guardian.Mother) {
                                    stringResource(R.string.guardian_mother)
                                } else {
                                    stringResource(R.string.guardian_father)
                                },
                            )
                        },
                    )
                }
            }
        }

        item {
            InfoActionCard(
                title = stringResource(R.string.shared_state_title),
            ) {
                Text(
                    stringResource(
                        R.string.shared_account_mode,
                        if (accountMode == AccountMode.Guest) {
                            stringResource(R.string.account_mode_guest)
                        } else {
                            stringResource(R.string.account_mode_account)
                        },
                    ),
                )
                Text(stringResource(R.string.shared_sync_state, stringResource(syncState.displayLabelRes())))
                Text(
                    text = if (profile.isShared) {
                        stringResource(R.string.shared_state_on)
                    } else {
                        stringResource(R.string.shared_state_off)
                    },
                )
                Button(onClick = onSimulateSync) {
                    Text(stringResource(R.string.shared_simulate_sync))
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.shared_activity_title),
                subtitle = stringResource(R.string.shared_activity_subtitle),
            )
        }

        items(profile.activityFeed) { entry ->
            BodyTextCard(text = entry.asString())
        }
    }
}
