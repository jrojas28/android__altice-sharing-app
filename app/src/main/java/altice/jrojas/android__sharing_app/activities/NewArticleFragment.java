package altice.jrojas.android__sharing_app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import altice.jrojas.android__sharing_app.MainActivity;
import altice.jrojas.android__sharing_app.R;
import altice.jrojas.android__sharing_app.classes.Article;
import altice.jrojas.android__sharing_app.classes.ArticleAdapter;
import altice.jrojas.android__sharing_app.classes.ArticleLocation;
import altice.jrojas.android__sharing_app.classes.User;

/**
 * Created by jaime on 6/11/2018.
 */

public class NewArticleFragment extends Fragment {
    private String TAG = "NEW_ARTICLE_FRAGMENT";
    //Firebase Related
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore database;
    //Class Variables
    private ImageView articleImage;
    private EditText articleTitle;
    private EditText articleDescription;
    private Button uploadArticleImageBtn;
    private Button createArticleBtn;
    private Button signInBtn;
    private OnFragmentInteractionListener mListener;
    private ImagePicker articleImagePicker;
    private Uri articleImageUri;

    public NewArticleFragment() {
        // Required empty public constructor
    }

    public static NewArticleFragment newInstance() {
        NewArticleFragment fragment = new NewArticleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.wtf(TAG, "On Create");
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("article_images");
        database = FirebaseFirestore.getInstance();
        articleImagePicker = new ImagePicker(
                getActivity(),
                this,
                new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {
                        Log.wtf(TAG, "Received image from ImagePicker");
                        Glide.with(getActivity()).load(imageUri).into(articleImage);
                        articleImageUri = imageUri;
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.wtf(TAG, "On Create View..");
        View newArticleView = inflater.inflate(R.layout.fragment_new_article, container, false);
        articleImage = newArticleView.findViewById(R.id.article_image);
        articleTitle = newArticleView.findViewById(R.id.article_title);
        articleDescription = newArticleView.findViewById(R.id.article_description);
        uploadArticleImageBtn = newArticleView.findViewById(R.id.article_image_upload_btn);
        uploadArticleImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setRelatedFragmentClass(NewArticleFragment.class);
                articleImagePicker.choosePicture(true);
            }
        });
        createArticleBtn = newArticleView.findViewById(R.id.article_create);
        createArticleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Upload to Firebase
                final Editable titleEditable = articleTitle.getText();
                final Editable descriptionEditable = articleDescription.getText();
                if(
                    titleEditable.length() != 0
                    && descriptionEditable.length() != 0
                    && articleImageUri != null
                ) {
                    uploadArticle(titleEditable.toString(), descriptionEditable.toString());
                }
                else {
                    Log.wtf(TAG, "Something's null");
                    Toast.makeText(getActivity(), "Todos los campos deben ser rellenados.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        signInBtn = newArticleView.findViewById(R.id.sign_in_error_button);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send to the Login Page.
                ((MainActivity) getActivity()).goToPage(2);
            }
        });
        Log.wtf(TAG, "Returning View..");
        return newArticleView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        ScrollView newArticleLayout = getView().findViewById(R.id.new_article);
        LinearLayout signInErrorLayout = getView().findViewById(R.id.sign_in_error);
        if(firebaseUser == null) {
            newArticleLayout.setVisibility(View.GONE);
            signInErrorLayout.setVisibility(View.VISIBLE);
        }
        else {
            newArticleLayout.setVisibility(View.VISIBLE);
            signInErrorLayout.setVisibility(View.GONE);
        }
    }

    public void onButtonPressed(Uri uri) {
        Log.wtf(TAG, "Button Pressed");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        articleImagePicker.handleActivityResult(resultCode, requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        articleImagePicker.handlePermission(requestCode, grantResults);
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

    //==============================================================================================
    //                                      Custom Methods
    //==============================================================================================

    public void uploadArticle(final String title,final String description) {
        Log.wtf(TAG, "Continuing to Firebase..");
        final MaterialDialog uploadProgress = new MaterialDialog.Builder(getActivity())
                .title("Subiendo imagen...")
                .content("Porfavor espere mientras se sube la imagen")
                .progress(false, 100)
                .build();
        uploadProgress.show();
        final String imageUUID = UUID.randomUUID().toString();
        UploadTask uploadTask = storageReference.child(imageUUID).putFile(articleImageUri);
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
                    final Uri downloadUri = task.getResult();
                    Location userLocation = ((MainActivity) getActivity()).getCurrentLocation();
                    ArticleLocation articleLocation = null;
                    if(userLocation != null) {
                        Geocoder geocoder = new Geocoder(getActivity());
                        List<Address> addressList = new ArrayList<>();
                        try {
                            addressList = geocoder.getFromLocation(
                                    userLocation.getLatitude(),
                                    userLocation.getLongitude(),
                                    1
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(addressList.size() > 0) {
                            Address userAddress = addressList.get(0);
                            articleLocation = new ArticleLocation(
                                    userLocation.getLatitude(),
                                    userLocation.getLongitude(),
                                    userAddress.getCountryName(),
                                    userAddress.getLocality()
                            );
                        }
                        else {
                            articleLocation = new ArticleLocation(
                                    userLocation.getLatitude(),
                                    userLocation.getLongitude(),
                                    "-",
                                    "-"
                            );
                        }

                        final Article article = new Article(
                                downloadUri.toString(),
                                title,
                                "author",
                                description,
                                0,
                                0,
                                articleLocation
                        );
                        //Lastly, we'll look for the user.
                        FirebaseUser fUser = firebaseAuth.getCurrentUser();
                        database.collection("users").document(fUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    //We found the user, so let's store it.
                                    DocumentSnapshot snap = task.getResult();
                                    article.setUser(snap.toObject(User.class));
                                }
                                else {
                                    //We couldn't obtain the user. Not much to do, just store the article.
                                }
                                //Time to store this shit
                                storeArticle(article);
                            }
                        });
                    }
                    else {
                        new MaterialDialog.Builder(getActivity())
                                .title("Introduzca la ubicacion del articulo")
                                .customView(R.layout.dialog_location_request, true)
                                .autoDismiss(false)
                                .positiveText("Enviar")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        View view = dialog.getView();
                                        Editable editableCountry = ((EditText) view.findViewById(R.id.article_country)).getText();
                                        Editable editableCity = ((EditText) view.findViewById(R.id.article_city)).getText();
                                        if(editableCountry.length() != 0 && editableCity.length() != 0) {
                                            ArticleLocation articleLocation = new ArticleLocation(
                                                editableCountry.toString(),
                                                editableCity.toString()
                                            );
                                            final Article article = new Article(
                                                downloadUri.toString(),
                                                title,
                                                "author",
                                                description,
                                                0,
                                                0,
                                                articleLocation
                                            );
                                            //Lastly, we'll look for the user.
                                            FirebaseUser fUser = firebaseAuth.getCurrentUser();
                                            database.collection("users").document(fUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        //We found the user, so let's store it.
                                                        DocumentSnapshot snap = task.getResult();
                                                        article.setUser(snap.toObject(User.class));
                                                    }
                                                    else {
                                                        //We couldn't obtain the user. Not much to do, just store the article.
                                                    }
                                                    //Time to store this shit
                                                    storeArticle(article);
                                                }
                                            });
                                            dialog.dismiss();
                                        }
                                        else {
                                            //Something is empty.
                                            Toast.makeText(getActivity(), "Tanto el pais como la ciudad son necesarios.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .build()
                                .show();
                    }
                }
                else {
                    //Something went wrong...
                    Toast.makeText(getActivity(), "Error al subir imagen. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void storeArticle(Article article) {
        DocumentReference ref = database.collection("articles").document();
        article.setId(ref.getId());
        ref.set(article).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    articleTitle.setText("");
                    articleDescription.setText("");
                    articleImageUri = null;
                    Glide.with(getActivity()).load(articleImageUri).into(articleImage);
                    Toast.makeText(getActivity(), "Su articulo ha sido agregado satisfactoriamente.", Toast.LENGTH_SHORT).show();
                    //Update articles
                    ((MainActivity) getActivity()).updateUI();
                }
                else {
                    Toast.makeText(getActivity(), "Ha ocurrido un problema conectando a la base de datos.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
