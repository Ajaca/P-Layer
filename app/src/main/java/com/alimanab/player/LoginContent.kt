package com.alimanab.player

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onRegister : () -> Unit,
    onLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }

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

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Account") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
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
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text("Register")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onLogin,
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
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }
    }
}