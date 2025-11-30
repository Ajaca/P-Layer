package com.alimanab.player

import com.alimanab.player.SQL
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext

lateinit var sqlManager: SQLManager
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onRegister : (String, String) -> Unit,
    onLogin: (String, String) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    sqlManager = SQLManager(context)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!isRegister) {
                    Text("Login", style = MaterialTheme.typography.headlineSmall)
                }
                else {
                    Text("Register", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Account") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                if (!isRegister) {
                    Row() {
                        Button(
                            onClick = { isRegister = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Register")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick =
                                {
                                    if (AccountManager.AccountVerify(email)
                                        && AccountManager.PasswordVerify(password)
                                        && sqlManager.login(email,password))
                                        onLogin(email,password)
                                    else
                                        Toast.makeText(context, "Invalid Account or password", Toast.LENGTH_SHORT).show()
                                },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text("Login")
                        }
                    }
                }
                else{
                    Row(){
                        Button(
                            onClick = { isRegister = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick =
                                {
                                    if (AccountManager.AccountVerify(email)
                                        && AccountManager.PasswordVerify(password)
                                        && sqlManager.register(email,password))
                                        onRegister(email,password)
                                    else
                                        Toast.makeText(context, "Invalid Account or password", Toast.LENGTH_SHORT).show()
                                },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }
    }
}