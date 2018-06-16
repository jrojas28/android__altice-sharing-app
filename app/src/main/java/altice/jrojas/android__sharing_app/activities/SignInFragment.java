package altice.jrojas.android__sharing_app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import altice.jrojas.android__sharing_app.MainActivity;
import altice.jrojas.android__sharing_app.R;
import altice.jrojas.android__sharing_app.classes.Article;
import altice.jrojas.android__sharing_app.classes.ArticleAdapter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import org.w3c.dom.Text;

import altice.jrojas.android__sharing_app.classes.ArticleAdapter;
import altice.jrojas.android__sharing_app.classes.ArticleLocation;
import altice.jrojas.android__sharing_app.classes.User;

/**
 * Created by jaime on 6/13/2018.
 */

public class SignInFragment extends Fragment {
    private String TAG = "SIGN_IN_FRAGMENT";
    private OnFragmentInteractionListener mListener;
    //Firebase Related
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    //Class Variables
    private LinearLayout logInContainer;
    private EditText logInEmail;
    private EditText logInPassword;
    private Button logInButton;
    private Button signUpButton;
    private ScrollView signUpContainer;
    private EditText signUpEmail;
    private EditText signUpUsername;
    private EditText signUpFirstName;
    private EditText signUpLastName;
    private EditText signUpPassword;
    private EditText signUpPasswordCheck;
    private TextView signUpEmailError;
    private TextView signUpUsernameError;
    private LinearLayout signUpPasswordErrorContainer;
    private Button signUpLogInButton;
    private Button signUpCreateButton;
    private LinearLayout profileContainer;
    private CircularImageView profilePicture;
    private ImagePicker profilePicturePicker;
    private FloatingActionButton profilePictureChangeButton;
    private TextView profileUsername;
    private RecyclerView profileArticleFeed;
    private ArticleAdapter profileArticleAdapter;
    private Toast errorToast;

    public SignInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SignInFragment.
     */
    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.wtf(TAG, "On Create");
        errorToast = Toast.makeText(getActivity(), "Ha ocurrido un error al intentar conectar con la base de datos.", Toast.LENGTH_SHORT);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("article_images");
        profilePicturePicker = new ImagePicker(
                getActivity(),
                this,
                new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {
                        final MaterialDialog uploadProgress = new MaterialDialog.Builder(getActivity())
                                .title("Subiendo imagen...")
                                .content("Porfavor espere mientras se sube la imagen")
                                .progress(false, 100)
                                .build();
                        uploadProgress.show();
                        final String imageUUID = UUID.randomUUID().toString();
                        UploadTask uploadTask = storageReference.child(imageUUID).putFile(imageUri);
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                uploadProgress.setMaxProgress((int) taskSnapshot.getTotalByteCount());
                                uploadProgress.setProgress((int) taskSnapshot.getBytesTransferred());
                                Log.wtf(TAG, String.valueOf(taskSnapshot.getTotalByteCount()));
                                Log.wtf(TAG, String.valueOf(taskSnapshot.getBytesTransferred()));
                            }
                        });
                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                uploadProgress.hide();
                                if(task.isSuccessful()) {
                                    return storageReference.child(imageUUID).getDownloadUrl();
                                }
                                //Something went wrong...
                                Toast.makeText(getActivity(), "Error al subir imagen. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                                return null;
                            };
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()) {
                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                    final Uri profilePictureUri = task.getResult();
                                    firebaseFirestore.collection("users")
                                            .document(firebaseUser.getUid())
                                            .update("profilePictureUrl", profilePictureUri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        //It uploaded correctly, so let's set it on our end too.
                                                        Glide.with(getView())
                                                                .load(profilePictureUri)
                                                                .apply(RequestOptions.centerCropTransform())
                                                                .into(profilePicture);
                                                    }
                                                }
                                            });
                                }
                                else {
                                    //Something went wrong...
                                    Toast.makeText(getActivity(), "Error al subir imagen. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
        ).setWithImageCrop(1,1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.wtf(TAG, "On Create View..");
        View signInView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        //Log In Section
        initializeLogIn(signInView);
        //Sign Up Section
        initializeSignUp(signInView);
        //Profile Section
        initializeProfile(signInView);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            //If the user is logged in, we'll need to update the feed w/ the articles he has created.
            logInContainer.setVisibility(View.GONE);
            signUpContainer.setVisibility(View.GONE);
            profileContainer.setVisibility(View.VISIBLE);
            updateProfile(user);
        }
        else {
            logInContainer.setVisibility(View.VISIBLE);
            signUpContainer.setVisibility(View.GONE);
            profileContainer.setVisibility(View.GONE);
        }
        Log.wtf(TAG, "Returning View..");
        return signInView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        profilePicturePicker.handleActivityResult(resultCode, requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        profilePicturePicker.handlePermission(requestCode, grantResults);
    }

    //==============================================================================================
    //                                      Custom Methods
    //==============================================================================================
    public void initializeLogIn(View signInView) {
        logInContainer = signInView.findViewById(R.id.sign_in_log_in_container);
        logInEmail = signInView.findViewById(R.id.log_in_email);
        logInPassword = signInView.findViewById(R.id.log_in_password);
        logInButton = signInView.findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable email = logInEmail.getText();
                Editable password = logInPassword.getText();
                if(email.length() > 0 && password.length() > 0) {
                    //Something's written on both sides, time to log in.
                    firebaseAuth.signInWithEmailAndPassword(email.toString(), password.toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        //The task was succesful
                                        //In this case, we'll call the method on main and rebuild from zero.
                                        ((MainActivity) getActivity()).updateUI(2);
                                    }
                                    else {
                                        //The task wasn't succesful, meaning something went wrong w/ the info provided.
                                        Toast.makeText(getActivity(), "Combinacion de Correo y Contrasena no encontrada.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
                }

            }
        });
        signUpButton = signInView.findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInContainer.setVisibility(View.GONE);
                profileContainer.setVisibility(View.GONE);
                signUpContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    public void initializeSignUp(View signInView) {
        signUpContainer = signInView.findViewById(R.id.sign_in_sign_up_container);
        signUpEmail = signInView.findViewById(R.id.sign_up_email);
        signUpEmailError = signInView.findViewById(R.id.sign_up_email_error);
        signUpUsername = signInView.findViewById(R.id.sign_up_username);
        signUpUsernameError = signInView.findViewById(R.id.sign_up_username_error);
        signUpFirstName = signInView.findViewById(R.id.sign_up_first_name);
        signUpLastName = signInView.findViewById(R.id.sign_up_last_name);
        signUpPassword = signInView.findViewById(R.id.sign_up_password);
        signUpPasswordCheck = signInView.findViewById(R.id.sign_up_password_check);
        signUpPasswordErrorContainer = signInView.findViewById(R.id.sign_up_password_errors);
        signUpLogInButton = signInView.findViewById(R.id.sign_up_log_in_button);
        signUpLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInContainer.setVisibility(View.VISIBLE);
                profileContainer.setVisibility(View.GONE);
                signUpContainer.setVisibility(View.GONE);
            }
        });
        signUpCreateButton = signInView.findViewById(R.id.sign_up_create_button);
        signUpCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //So here we start checking everything.
                //Initially, we'll want to clear the errors.
                signUpEmailError.setVisibility(View.GONE);
                signUpUsernameError.setVisibility(View.GONE);
                signUpPasswordErrorContainer.setVisibility(View.GONE);
                signUpPasswordErrorContainer.removeAllViews();
                //Now that we got that cleaned up, it's time to check the strings.
                Editable editableEmail = signUpEmail.getText();
                Editable editableUsername = signUpUsername.getText();
                Editable editableFirstName = signUpFirstName.getText();
                Editable editableLastName = signUpLastName.getText();
                Editable editablePassword = signUpPassword.getText();
                Editable editablePasswordCheck = signUpPasswordCheck.getText();
                if(
                        editableEmail.length() > 0
                                && editableUsername.length() > 0
                                && editableFirstName.length() > 0
                                && editableLastName.length() > 0
                                && editablePassword.length() > 0
                                && editablePasswordCheck.length() > 0
                        ) {
                    //All of the fields are filled. Time to check on them.
                    final String email = editableEmail.toString();
                    final String username = editableUsername.toString();
                    final String firstName = editableFirstName.toString();
                    final String lastName = editableLastName.toString();
                    final String password = editablePassword.toString();
                    final String passwordCheck = editablePasswordCheck.toString();
                    //First, we need to check if the email or username has been used before.
                    firebaseFirestore.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                //Task is successful
                                QuerySnapshot snap = task.getResult();
                                List<User> usersWithEmail = snap.toObjects(User.class);
                                if(usersWithEmail.size() > 0) {
                                    //An user exists with this email.
                                    signUpEmailError.setVisibility(View.VISIBLE);
                                }
                                else {
                                    //If no user exists, we check for username.
                                    firebaseFirestore.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()) {
                                                QuerySnapshot snap = task.getResult();
                                                List<User> usersWithUsername = snap.toObjects(User.class);
                                                if(usersWithUsername.size() > 0) {
                                                    //An user exists with this username
                                                    signUpUsernameError.setVisibility(View.VISIBLE);
                                                }
                                                else {
                                                    //Otherwise, we can proceed to validate password and finally create this.
                                                    ArrayList<String> errors = (ArrayList<String>) User.passwordVerification(password, passwordCheck);
                                                    if(errors.size() > 0) {
                                                        //There are errors regarding the password, so tell that to the user.
                                                        signUpPasswordErrorContainer.setVisibility(View.VISIBLE);
                                                        for(String error : errors) {
                                                            TextView tv = new TextView(getActivity());
                                                            tv.setText(error);
                                                            tv.setTextColor(Color.RED);
                                                            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                                                            signUpPasswordErrorContainer.addView(tv, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                        }
                                                    }
                                                    else {
                                                        //Everything appears to be valid, so let's try creating the user.
                                                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if(task.isSuccessful()) {
                                                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                                                    //User was created on Firebase, so we will write a record for our users on a collection.
                                                                    User user = new User(firebaseUser.getUid(), email, username, firstName, lastName);
                                                                    firebaseFirestore.collection("users").document(firebaseUser.getUid()).set(user);
                                                                    Toast.makeText(getActivity(), "Usuario registrado.", Toast.LENGTH_SHORT).show();
                                                                    ((MainActivity) getActivity()).updateUI();
                                                                }
                                                                else {
                                                                    //If task wasn't succesful, we can't continue.
                                                                    errorToast.show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            }
                                            else {
                                                //If task wasn't succesful, we can't continue.
                                                errorToast.show();
                                            }
                                        }
                                    });
                                }
                            }
                            else {
                                //If task wasn't succesful, we can't continue.
                                errorToast.show();
                            }
                        }
                    });
                }
                else {
                    //If this happens, not all fields are filled.
                    Toast.makeText(getActivity(), "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initializeProfile(View signInView) {
        profileContainer = signInView.findViewById(R.id.sign_in_profile_container);
        profilePicture = signInView.findViewById(R.id.profile_picture);
        profileUsername = signInView.findViewById(R.id.profile_username);
        profilePictureChangeButton = signInView.findViewById(R.id.profile_picture_upload);
        profilePictureChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePicturePicker.choosePicture(true);
            }
        });
        profileArticleAdapter = new ArticleAdapter();
        profileArticleFeed = signInView.findViewById(R.id.profile_article_feed);
        profileArticleFeed.setAdapter(profileArticleAdapter);
        profileArticleFeed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void updateProfile(FirebaseUser user) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference databaseUser = firebaseFirestore.collection("users").document(user.getUid());
        databaseUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    //The task is succesful, so we can work with the document.
                    DocumentSnapshot userSnap = task.getResult();
                    User user = userSnap.toObject(User.class);
                    if(user != null) {
                        profileUsername.setText(user.getUsername());
                        String url = user.getProfilePictureUrl();
                        if(url != null && getView() != null) {
                            Glide.with(getView()).load(url).into(profilePicture);
                        }
                        firebaseFirestore.collection("articles")
                                .whereEqualTo("user.id", user.getId())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()) {
                                            QuerySnapshot articlesQuery = task.getResult();
                                            profileArticleAdapter.updateData(articlesQuery.toObjects(Article.class));
                                        }
                                        else {
                                            errorToast.show();
                                        }
                                    }
                                });
                    }
                }
                else {
                    //There was a problem obtaining the document for the user.
                    errorToast.show();
                }
            }
        });

    }
}
