package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrimsRepository
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun AdminManagementSection(
    repository: ScrimsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminUsers by repository.adminUsers.collectAsState()
    val invitations by repository.invitations.collectAsState()

    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedAdminForEdit by remember { mutableStateOf<AdminUser?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_management_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Staff & Role-Based Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Manage tournament referees, finance mods, and staff permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showInviteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("invite_staff_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Invite Staff", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Admins List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items(adminUsers, key = { it.id }) { admin ->
                AdminUserCard(
                    admin = admin,
                    onEditClick = { selectedAdminForEdit = admin }
                )
            }

            if (invitations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PENDING INVITATIONS (${invitations.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                items(invitations, key = { it.id }) { inv ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = inv.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${inv.role.label} • Code: ${inv.code}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricBlue
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WarningOrange.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = inv.status.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningOrange,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteAdminDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = { email, role, perms ->
                val inv = repository.inviteAdmin(email, role, perms)
                Toast.makeText(context, "Invitation code ${inv.code} generated for $email", Toast.LENGTH_LONG).show()
                showInviteDialog = false
            }
        )
    }

    selectedAdminForEdit?.let { admin ->
        EditAdminPermissionsDialog(
            admin = admin,
            onDismiss = { selectedAdminForEdit = null },
            onSave = { updatedRole, updatedPerms, updatedStatus ->
                repository.updateAdminPermissions(admin.id, updatedRole, updatedPerms)
                repository.setAdminStatus(admin.id, updatedStatus)
                Toast.makeText(context, "Permissions updated for ${admin.email}", Toast.LENGTH_SHORT).show()
                selectedAdminForEdit = null
            }
        )
    }
}

@Composable
fun AdminUserCard(admin: AdminUser, onEditClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = admin.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (admin.isSuperAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CrimsonPrimary)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SUPER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${admin.role.label} • ${admin.permissions.size} active permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent
                )
                Text(
                    text = "Status: ${admin.status.label} • Active: ${admin.lastActiveNepal}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (admin.status == AdminStatus.ACTIVE) SuccessGreen else ErrorRed
                )
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.testTag("edit_admin_${admin.id}")
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Edit Permissions",
                    tint = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteAdminDialog(
    onDismiss: () -> Unit,
    onInvite: (String, AdminRole, List<AdminPermission>) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AdminRole.TOURNAMENT_MOD) }
    var selectedPermissions by remember {
        mutableStateOf(AdminPermission.moderatorDefaults().toSet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Invite Staff Administrator", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Staff Email Address") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("invite_email_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Assign Role:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                AdminRole.entries.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRole = role
                                selectedPermissions = when (role) {
                                    AdminRole.SUPER_ADMIN -> AdminPermission.all().toSet()
                                    AdminRole.TOURNAMENT_MOD -> AdminPermission.moderatorDefaults().toSet()
                                    AdminRole.FINANCE_MOD -> AdminPermission.financeDefaults().toSet()
                                    AdminRole.SUPPORT_AGENT -> AdminPermission.supportDefaults().toSet()
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = {
                                selectedRole = role
                                selectedPermissions = when (role) {
                                    AdminRole.SUPER_ADMIN -> AdminPermission.all().toSet()
                                    AdminRole.TOURNAMENT_MOD -> AdminPermission.moderatorDefaults().toSet()
                                    AdminRole.FINANCE_MOD -> AdminPermission.financeDefaults().toSet()
                                    AdminRole.SUPPORT_AGENT -> AdminPermission.supportDefaults().toSet()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = role.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Permissions Checklist:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                AdminPermission.entries.forEach { perm ->
                    val isChecked = selectedPermissions.contains(perm)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPermissions = if (isChecked) selectedPermissions - perm else selectedPermissions + perm
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedPermissions = if (checked) selectedPermissions + perm else selectedPermissions - perm
                            },
                            colors = CheckboxDefaults.colors(checkedColor = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = perm.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onInvite(email.trim(), selectedRole, selectedPermissions.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.testTag("confirm_invite_btn")
            ) {
                Text("Generate Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
}

@Composable
fun EditAdminPermissionsDialog(
    admin: AdminUser,
    onDismiss: () -> Unit,
    onSave: (AdminRole, List<AdminPermission>, AdminStatus) -> Unit
) {
    var selectedRole by remember { mutableStateOf(admin.role) }
    var selectedStatus by remember { mutableStateOf(admin.status) }
    var selectedPermissions by remember { mutableStateOf(admin.permissions.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Staff: ${admin.email}", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Role & Status", fontWeight = FontWeight.Bold, color = GoldAccent)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatus.entries.forEach { status ->
                        val isSelected = selectedStatus == status
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CrimsonPrimary else BrandCardElevated)
                                .clickable { selectedStatus = status }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Permissions", fontWeight = FontWeight.Bold, color = GoldAccent)

                AdminPermission.entries.forEach { perm ->
                    val isChecked = selectedPermissions.contains(perm)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPermissions = if (isChecked) selectedPermissions - perm else selectedPermissions + perm
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selectedPermissions = if (checked) selectedPermissions + perm else selectedPermissions - perm
                            },
                            colors = CheckboxDefaults.colors(checkedColor = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = perm.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRole, selectedPermissions.toList(), selectedStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
}
