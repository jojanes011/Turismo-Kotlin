package com.example.turismokotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.properties.Delegates

class RegisterActivity : AppCompatActivity() {
    private lateinit var txtName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    //global variables
    private var firstName by Delegates.notNull<String>()
    private var lastName by Delegates.notNull<String>()
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro)
        initialise()

    }

/*Creamos un método para inicializar nuestros elementos del diseño y la autenticación y la base de datos de firebase*/

    private fun initialise() {
        //llamamos nuestras vista
        txtName = findViewById(R.id.txtName)
        txtLastName = findViewById(R.id.txtLastName)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        //Creamos nuestro progressDialog
        progressBar = ProgressDialog(this)

/*Creamos una instancia para guardar los datos del usuario en nuestra base  de datos*/
        database = FirebaseDatabase.getInstance()
/*Creamos una instancia para crear nuestra autenticación y guardar el usuario*/
        auth = FirebaseAuth.getInstance()

/*reference nosotros leemos o escribimos en una ubicación específica la base de datos*/
        databaseReference = database.reference.child("Users")
    }

    //Vamos a crear nuestro método para crear una nueva cuenta
    private fun createNewAccount() {

        //Obtenemos los datos de nuestras cajas de texto
        firstName = txtName.text.toString()
        lastName = txtLastName.text.toString()
        email = txtEmail.text.toString()
        password = txtPassword.text.toString()

//Verificamos que los campos estén llenos
        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
            && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
        ) {

/*Antes de iniciar nuestro registro bloqueamos la pantalla o también podemos usar una barra de proceso por lo que progressbar está obsoleto*/

            progressBar.setMessage("Usuario registrado...")
            progressBar.show()

//vamos a dar de alta el usuario con el correo y la contraseña
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {

//Si está en este método quiere decir que todo salio bien en la autenticación

/*Una vez que se dio de alta la cuenta vamos a dar de alta la información en la base de datos*/

/*Vamos a obtener el id del usuario con que accedio con currentUser*/
                    val user: FirebaseUser = auth.currentUser!!
//enviamos email de verificación a la cuenta del usuario
                    verifyEmail(user);
/*Damos de alta la información del usuario enviamos el la referencia para guardarlo en la base de datos  de preferencia enviamos el id para que no se repita*/
                    val currentUserDb = databaseReference.child(user.uid)
//Agregamos el nombre y el apellido dentro de user/id/
                    currentUserDb.child("firstName").setValue(firstName)
                    currentUserDb.child("lastName").setValue(lastName)
//Por último nos vamos a la vista home
                    updateUserInfoAndGoHome()

                }.addOnFailureListener {
// si el registro falla se mostrara este mensaje
                    Toast.makeText(
                        this, "Error en la autenticación.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        } else {
            Toast.makeText(this, "Llene todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    //llamamos el método de crear cuenta en la accion registrar
    fun register(view: View) {
        createNewAccount()
    }

    private fun updateUserInfoAndGoHome() {
        //Nos vamos a la actividad home
        val intent = Intent(this, Sitios::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
//ocultamos el progress
        progressBar.hide()

    }

    private fun verifyEmail(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener(this) {
//Verificamos que la tarea se realizó correctamente
                    task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Email " + user.getEmail(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar el correo ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}

