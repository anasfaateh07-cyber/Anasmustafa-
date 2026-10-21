package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FirebaseUserData(
    val uid: String,
    val email: String,
    val name: String,
    val role: String,
    val assignedGroups: List<String> = emptyList()
)

data class FirestoreUserRecord(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "pending",
    val requestedRole: String = "",
    val groups: List<String> = emptyList(),
    val studentIds: List<String> = emptyList()
)

data class FirestoreStudentItem(
    val id: String = "",
    val name: String = "",
    val className: String = "",
    val department: String = "",
    val fatherName: String = ""
)

class FirebaseAuthRepository(private val context: Context) {

    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun hasValidFirebaseApiKey(): Boolean {
        val resId = context.resources.getIdentifier("google_api_key", "string", context.packageName)
        if (resId != 0) {
            val key = context.getString(resId).trim()
            if (key.isNotBlank() && !key.startsWith("AIzaSyPlaceholder") && !key.contains("Placeholder")) {
                return true
            }
        }
        return false
    }

    fun isOnlineFirebaseAvailable(): Boolean = auth != null && firestore != null && hasValidFirebaseApiKey()

    private fun resolveRequestedRole(selectedRole: String): String {
        val clean = selectedRole.removePrefix("As a ").removePrefix("As an ").trim().lowercase()
        return when (clean) {
            "teacher" -> "teacher"
            "parent" -> "parent"
            "student" -> "student"
            "principal" -> "principal"
            "admin" -> "admin"
            else -> clean.ifEmpty { "teacher" }
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String, selectedRole: String = "Teacher"): FirebaseUserData {
        val safeAuth = auth ?: throw IllegalStateException("Firebase Auth not initialized")
        val safeDb = firestore ?: throw IllegalStateException("Firestore not initialized")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = safeAuth.signInWithCredential(credential).await()
        val user = authResult.user ?: throw IllegalStateException("Google sign in failed: no user returned.")

        val requestedRole = resolveRequestedRole(selectedRole)
        val userDocRef = safeDb.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            val userEmail = user.email ?: ""
            val resolvedName = user.displayName?.ifBlank { null }
                ?: (if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "User")
            val newUserData = hashMapOf(
                "role" to "pending",
                "requestedRole" to requestedRole,
                "email" to userEmail,
                "name" to resolvedName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(newUserData).await()
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        // Document exists: do not change its role
        val dbRole = snapshot.getString("role")?.trim() ?: "pending"
        val lowerRole = dbRole.lowercase()

        if (lowerRole == "pending" || lowerRole.isBlank()) {
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        val isAdminAttempt = requestedRole == "admin" || requestedRole == "principal"
        if (isAdminAttempt) {
            val isUserAdmin = lowerRole == "admin" || lowerRole == "principal"
            if (!isUserAdmin) {
                safeAuth.signOut()
                throw SecurityException("This account is not an admin")
            }
        }

        val effectiveRole = when {
            lowerRole == "admin" || lowerRole == "principal" -> "Admin"
            lowerRole == "teacher" -> "Teacher"
            lowerRole == "parent" -> "Parent"
            lowerRole == "student" -> "Student"
            else -> dbRole.replaceFirstChar { it.uppercase() }
        }

        @Suppress("UNCHECKED_CAST")
        val assigned = (snapshot.get("assignedGroups") as? List<String>)
            ?: (snapshot.get("groups") as? List<String>)
            ?: emptyList()

        val cleanEmail = user.email ?: snapshot.getString("email") ?: ""
        val resolvedName = snapshot.getString("name")?.ifBlank {
            user.displayName ?: cleanEmail.substringBefore("@")
        } ?: (user.displayName ?: cleanEmail.substringBefore("@"))

        return FirebaseUserData(
            uid = user.uid,
            email = cleanEmail,
            name = resolvedName,
            role = effectiveRole,
            assignedGroups = assigned
        )
    }

    suspend fun signUpWithEmail(email: String, pass: String, selectedRole: String, name: String = ""): FirebaseUserData {
        val safeAuth = auth ?: throw IllegalStateException("Firebase Auth is not available. Please check network connection.")
        val safeDb = firestore ?: throw IllegalStateException("Firestore is not available.")

        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) throw IllegalArgumentException("Email address cannot be empty.")
        if (pass.length < 6) throw IllegalArgumentException("Password must be at least 6 characters.")

        val requestedRole = resolveRequestedRole(selectedRole)
        if (requestedRole == "admin") {
            throw SecurityException("Admin accounts cannot be registered publicly. They must be designated by an administrator.")
        }

        val authResult = safeAuth.createUserWithEmailAndPassword(cleanEmail, pass.trim()).await()
        val user = authResult.user ?: throw IllegalStateException("Sign up failed: no user returned.")

        val userEmail = user.email ?: cleanEmail
        val resolvedName = name.trim().ifBlank {
            user.displayName?.ifBlank { null } ?: (if (userEmail.isNotBlank()) userEmail.substringBefore("@") else cleanEmail.substringBefore("@"))
        }

        val userDocRef = safeDb.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            val initialData = hashMapOf(
                "role" to "pending",
                "requestedRole" to requestedRole,
                "email" to userEmail,
                "name" to resolvedName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(initialData).await()
        }

        safeAuth.signOut()
        throw SecurityException("Your account is waiting for admin approval")
    }

    suspend fun signInWithEmail(email: String, pass: String, selectedRole: String = "Teacher"): FirebaseUserData {
        val safeAuth = auth ?: throw IllegalStateException("Firebase Auth is not available. Please check network connection.")
        val safeDb = firestore ?: throw IllegalStateException("Firestore is not available.")

        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) throw IllegalArgumentException("Please enter your email address.")
        if (pass.isBlank()) throw IllegalArgumentException("Please enter your password.")

        val requestedRole = resolveRequestedRole(selectedRole)
        val authResult = safeAuth.signInWithEmailAndPassword(cleanEmail, pass.trim()).await()
        val user = authResult.user ?: throw IllegalStateException("Login failed: no user returned.")

        val userDocRef = safeDb.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            val userEmail = user.email ?: cleanEmail
            val resolvedName = user.displayName?.ifBlank { null }
                ?: (if (userEmail.isNotBlank()) userEmail.substringBefore("@") else cleanEmail.substringBefore("@"))
            val newUserData = hashMapOf(
                "role" to "pending",
                "requestedRole" to requestedRole,
                "email" to userEmail,
                "name" to resolvedName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(newUserData).await()
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        // Document exists: do not change its role
        val dbRole = snapshot.getString("role")?.trim() ?: "pending"
        val lowerRole = dbRole.lowercase()

        if (lowerRole == "pending" || lowerRole.isBlank()) {
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        val isAdminAttempt = requestedRole == "admin" || requestedRole == "principal"
        if (isAdminAttempt) {
            val isUserAdmin = lowerRole == "admin" || lowerRole == "principal"
            if (!isUserAdmin) {
                safeAuth.signOut()
                throw SecurityException("This account is not an admin")
            }
        }

        val effectiveRole = when {
            lowerRole == "admin" || lowerRole == "principal" -> "Admin"
            lowerRole == "teacher" -> "Teacher"
            lowerRole == "parent" -> "Parent"
            lowerRole == "student" -> "Student"
            else -> dbRole.replaceFirstChar { it.uppercase() }
        }

        @Suppress("UNCHECKED_CAST")
        val assigned = (snapshot.get("assignedGroups") as? List<String>)
            ?: (snapshot.get("groups") as? List<String>)
            ?: emptyList()

        val resolvedEmail = user.email ?: cleanEmail
        val resolvedName = snapshot.getString("name")?.ifBlank {
            user.displayName ?: resolvedEmail.substringBefore("@")
        } ?: (user.displayName ?: resolvedEmail.substringBefore("@"))

        return FirebaseUserData(
            uid = user.uid,
            email = resolvedEmail,
            name = resolvedName,
            role = effectiveRole,
            assignedGroups = assigned
        )
    }

    suspend fun updateTeacherAssignedGroupsInFirestore(teacherIdentifier: String, groups: List<String>) {
        val safeDb = firestore ?: return
        runCatching {
            val docRef = safeDb.collection("users").document(teacherIdentifier)
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update("assignedGroups", groups).await()
            } else {
                val querySnap = safeDb.collection("users").whereEqualTo("email", teacherIdentifier).get().await()
                for (d in querySnap.documents) {
                    d.reference.update("assignedGroups", groups).await()
                }
            }
        }.onFailure {
            Log.e("FirebaseAuthRepo", "Failed to update assigned groups for $teacherIdentifier: ${it.message}")
        }
    }

    fun listenToAssignedGroups(uid: String, onUpdate: (List<String>) -> Unit): ListenerRegistration? {
        val safeDb = firestore ?: return null
        return runCatching {
            safeDb.collection("users").document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val currentDoc = snapshot ?: return@addSnapshotListener
                if (!currentDoc.exists()) return@addSnapshotListener
                @Suppress("UNCHECKED_CAST")
                val groups = (currentDoc.get("assignedGroups") as? List<String>) ?: emptyList()
                onUpdate(groups)
            }
        }.getOrNull()
    }

    fun signOut() {
        runCatching { auth?.signOut() }
    }

    suspend fun checkCurrentUserSession(): FirebaseUserData? {
        val safeAuth = auth ?: return null
        val safeDb = firestore ?: return null
        val currentUser = safeAuth.currentUser ?: return null

        val userDocRef = safeDb.collection("users").document(currentUser.uid)
        val snapshot = userDocRef.get().await()
        if (!snapshot.exists()) {
            val userEmail = currentUser.email ?: ""
            val resolvedName = currentUser.displayName?.ifBlank { null }
                ?: (if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "User")
            val newUserData = hashMapOf(
                "role" to "pending",
                "requestedRole" to "teacher",
                "email" to userEmail,
                "name" to resolvedName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            userDocRef.set(newUserData).await()
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        val dbRole = snapshot.getString("role")?.trim() ?: "pending"
        val lowerRole = dbRole.lowercase()

        if (lowerRole == "pending" || lowerRole.isBlank()) {
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        val isApproved = lowerRole == "admin" || lowerRole == "principal" ||
                lowerRole == "teacher" || lowerRole == "parent" || lowerRole == "student"

        if (!isApproved) {
            safeAuth.signOut()
            throw SecurityException("Your account is waiting for admin approval")
        }

        val effectiveRole = when {
            lowerRole == "admin" || lowerRole == "principal" -> "Admin"
            lowerRole == "teacher" -> "Teacher"
            lowerRole == "parent" -> "Parent"
            lowerRole == "student" -> "Student"
            else -> dbRole.replaceFirstChar { it.uppercase() }
        }

        @Suppress("UNCHECKED_CAST")
        val assigned = (snapshot.get("assignedGroups") as? List<String>) ?: emptyList()

        val cleanEmail = currentUser.email ?: snapshot.getString("email") ?: ""
        val resolvedName = snapshot.getString("name")?.ifBlank {
            currentUser.displayName ?: cleanEmail.substringBefore("@")
        } ?: (currentUser.displayName ?: cleanEmail.substringBefore("@"))

        return FirebaseUserData(
            uid = currentUser.uid,
            email = cleanEmail,
            name = resolvedName,
            role = effectiveRole,
            assignedGroups = assigned
        )
    }

    fun getDefaultWebClientId(): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val resStr = context.getString(resId).trim()
            if (resStr.isNotBlank() && !resStr.startsWith("123456789012")) {
                return resStr
            }
        }
        return "835145873453-rdaki8mpma5us15rbtb8mopjhid4a4e7.apps.googleusercontent.com"
    }

    suspend fun launchGoogleSignIn(activityContext: Context, explicitServerClientId: String? = null): GoogleSignInResult {
        if (!hasValidFirebaseApiKey()) {
            return GoogleSignInResult.Error(
                "[invalid-api-key] Google Sign-In requires your real google-services.json from Firebase Console. Please add it to app/ folder."
            )
        }
        val serverClientId = explicitServerClientId ?: getDefaultWebClientId()
        if (serverClientId.isNullOrBlank()) {
            return GoogleSignInResult.Error(
                "[missing-client-id] Missing Web Client ID (default_web_client_id). Please add your real google-services.json from Firebase Console to app/ folder."
            )
        }
        val safeClientId: String = serverClientId

        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(safeClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = activityContext, request = request)
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                GoogleSignInResult.Success(credential.idToken)
            } else {
                GoogleSignInResult.Error("[unsupported-credential] Unsupported credential type returned from Google.")
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            GoogleSignInResult.Error("[no-credentials] No Google accounts found or user cancelled account selection.")
        } catch (e: GetCredentialException) {
            Log.e("FirebaseAuthRepo", "CredentialManager failed: ${e.message}")
            GoogleSignInResult.Error("[credential-error] Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "Sign-in exception: ${e.message}")
            GoogleSignInResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    /**
     * Observes pending users using Firestore query: where("role", "==", "pending")
     */
    fun getPendingUsersFlow(): Flow<List<FirestoreUserRecord>> = callbackFlow {
        val safeDb = firestore
        val currentUid = auth?.currentUser?.uid
        if (safeDb == null || currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = safeDb.collection("users")
            .whereEqualTo("role", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseAuthRepo", "Pending users listener note: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    parseFirestoreUserRecord(doc)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observes all users in the users collection for the admin management view
     */
    fun getAllUsersFlow(): Flow<List<FirestoreUserRecord>> = callbackFlow {
        val safeDb = firestore
        val currentUid = auth?.currentUser?.uid
        if (safeDb == null || currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = safeDb.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseAuthRepo", "All users listener note: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    parseFirestoreUserRecord(doc)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    private fun parseFirestoreUserRecord(doc: com.google.firebase.firestore.DocumentSnapshot): FirestoreUserRecord {
        val uid = doc.id
        val name = doc.getString("name")?.ifBlank { null }
            ?: doc.getString("displayName")?.ifBlank { null }
            ?: doc.getString("email")?.substringBefore("@")
            ?: "User"
        val email = doc.getString("email") ?: ""
        val role = doc.getString("role")?.trim() ?: "pending"
        val requestedRole = doc.getString("requestedRole")?.trim() ?: ""

        val groups: List<String> = (doc.get("groups") as? List<*>)?.mapNotNull { it?.toString() }
            ?: (doc.get("assignedGroups") as? List<*>)?.mapNotNull { it?.toString() }
            ?: emptyList()

        val studentIds: List<String> = (doc.get("studentIds") as? List<*>)?.mapNotNull { it?.toString() }
            ?: (doc.get("assignedStudentIds") as? List<*>)?.mapNotNull { it?.toString() }
            ?: emptyList()

        return FirestoreUserRecord(
            uid = uid,
            name = name,
            email = email,
            role = role,
            requestedRole = requestedRole,
            groups = groups,
            studentIds = studentIds
        )
    }

    /**
     * Fetches students from Firestore students collection
     */
    suspend fun fetchStudentsFromFirestore(): List<FirestoreStudentItem> {
        val safeDb = firestore ?: return emptyList()
        if (auth?.currentUser == null) return emptyList()
        return try {
            val snap = safeDb.collection("students").get().await()
            snap.documents.map { doc ->
                FirestoreStudentItem(
                    id = doc.id,
                    name = doc.getString("name") ?: doc.getString("studentName") ?: doc.id,
                    className = doc.getString("className") ?: "",
                    department = doc.getString("department") ?: "",
                    fatherName = doc.getString("fatherName") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthRepo", "Failed to fetch students from Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * Ensures local students exist in Firestore students collection so admin search finds them
     */
    suspend fun syncLocalStudentsToFirestore(students: List<StudentEntity>) {
        val safeDb = firestore ?: return
        if (auth?.currentUser == null) return
        try {
            students.forEach { st ->
                val docId = st.id.toString()
                val data = hashMapOf(
                    "name" to st.name,
                    "fatherName" to st.fatherName,
                    "className" to st.className,
                    "department" to st.department,
                    "monthlyFee" to st.monthlyFee,
                    "phone" to st.phone
                )
                safeDb.collection("students").document(docId).set(data).await()
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthRepo", "Local student sync notice: ${e.message}")
        }
    }

    /**
     * Admin-only user approval & role updates.
     * Saving writes role, groups or studentIds to users/{uid}. Do not change any other field.
     * Shows real Firebase error code if write fails.
     */
    suspend fun updateUserApproval(
        targetUid: String,
        newRole: String,
        groups: List<String>,
        studentIds: List<String>
    ): Result<Unit> {
        val safeAuth = auth ?: return Result.failure(IllegalStateException("auth/not-initialized"))
        val safeDb = firestore ?: return Result.failure(IllegalStateException("firestore/not-initialized"))
        val currentUid = safeAuth.currentUser?.uid ?: return Result.failure(IllegalStateException("auth/no-current-user"))

        // Security check: Must have role "admin" (or "principal") in users/{uid} or root admin email
        val isRootAdminEmail = safeAuth.currentUser?.email?.equals("qarimanasmustafa@gmail.com", ignoreCase = true) == true
        val adminDoc = safeDb.collection("users").document(currentUid).get().await()
        val adminRole = adminDoc.getString("role")?.trim()?.lowercase() ?: ""
        if (!isRootAdminEmail && adminRole != "admin" && adminRole != "principal") {
            return Result.failure(SecurityException("permission-denied: Only an administrator can approve or modify user roles."))
        }

        val normalizedRole = newRole.lowercase().trim()
        val updates = mutableMapOf<String, Any>()
        updates["role"] = normalizedRole

        if (normalizedRole == "teacher") {
            updates["groups"] = groups
            updates["assignedGroups"] = groups
        } else if (normalizedRole == "parent" || normalizedRole == "student") {
            updates["studentIds"] = studentIds
            updates["assignedStudentIds"] = studentIds
        }

        return try {
            safeDb.collection("users").document(targetUid).update(updates).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("${e.code.name}: ${e.message ?: "Firestore write error"}"))
        } catch (e: FirebaseException) {
            Result.failure(Exception("${e.message ?: "Firebase write failed"}"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Update failed"))
        }
    }
}

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}

