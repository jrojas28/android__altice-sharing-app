package altice.jrojas.android__sharing_app.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import altice.jrojas.android__sharing_app.R;
import altice.jrojas.android__sharing_app.classes.Article;
import altice.jrojas.android__sharing_app.classes.ArticleAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArticleFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArticleFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleFeedFragment extends Fragment {
    private String TAG = "ARTICLE_FEED_FRAGMENT";
    private OnFragmentInteractionListener mListener;
    //Firebase Related
    private FirebaseFirestore firebaseFirestore;
    //Class Variables
    private RecyclerView articleFeed;
    private ArticleAdapter articleAdapter;
    private LinearLayout errorContainer;
    private Button retryButton;

    public ArticleFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArticleFeedFragment.
     */
    public static ArticleFeedFragment newInstance() {
        ArticleFeedFragment fragment = new ArticleFeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.wtf(TAG, "On Create");
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("articles").orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    //Something went wrong..
                    Toast.makeText(getActivity(), "Error actualizando articulos.", Toast.LENGTH_SHORT).show();
                }
                else {
                    articleAdapter.updateData(queryDocumentSnapshots.toObjects(Article.class));
                    Toast.makeText(getActivity(), "Hay nuevos articulos sin leer.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.wtf(TAG, "On Create View..");
        View articleFeedView = inflater.inflate(R.layout.fragment_article_feed, container, false);
        articleAdapter = new ArticleAdapter();
        articleFeed = articleFeedView.findViewById(R.id.article_feed);
        articleFeed.setAdapter(articleAdapter);
        articleFeed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        errorContainer = articleFeedView.findViewById(R.id.article_feed_error);
        retryButton = articleFeedView.findViewById(R.id.article_feed_error_btn);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedAdapter();
            }
        });
        feedAdapter();
        Log.wtf(TAG, "Returning View..");
        return articleFeedView;
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

    //==============================================================================================
    //                                      Custom Methods
    //==============================================================================================
    public void feedAdapter() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("articles").orderBy("createdAt", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    //Query was successful
                    articleFeed.setVisibility(View.VISIBLE);
                    errorContainer.setVisibility(View.GONE);
                    QuerySnapshot snap = task.getResult();
                    articleAdapter.updateData(snap.toObjects(Article.class));
                }
                else {
                    //Query wasn't sucessful
                    articleFeed.setVisibility(View.GONE);
                    errorContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    public void feedExampleAdapter() {
        ArrayList<Article> articles = new ArrayList<>();
        articles.add(new Article("https://cdn.pixabay.com/photo/2016/06/18/17/42/image-1465348_960_720.jpg", "Some random statue"));
        articles.add(new Article("https://www.gannett-cdn.com/-mm-/2001d7f151d3ce6c86736745231d67fc707f6c92/c=41-0-481-331&r=x404&c=534x401/local/-/media/2018/05/07/MIGroup/BattleCreek/636613035474588578-642567592-1-.jpg", "Some aurora"));
        articleAdapter.updateData(articles);
    }
}
