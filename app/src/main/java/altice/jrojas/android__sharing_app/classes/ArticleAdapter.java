package altice.jrojas.android__sharing_app.classes;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import altice.jrojas.android__sharing_app.MainActivity;
import altice.jrojas.android__sharing_app.R;

/**
 * Created by jaime on 6/3/2018.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>{
    private static final String TAG = "ARTICLE_ADAPTER";
    private ArrayList<Article> articles;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private User loggedUser;

    public ArticleAdapter() {
        articles = new ArrayList<Article>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {
            firebaseFirestore.collection("users")
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot snap = task.getResult();
                                loggedUser = snap.toObject(User.class);
                            }
                        }
                    });
        }
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ArticleViewHolder holder, int position) {
        final Article article = articles.get(position);
        //Here we must fill the article view holder with the contents.
        //Image
        Glide.with(holder.itemView).load(article.getImageUrl()).into(holder.getArticleImage());
        //Text
        holder.getArticleDocumentId().setText(article.getId());
        Log.wtf(TAG, article.getId());
        holder.getArticleAuthor().setText("Por " + article.getAuthor());
        holder.getArticleTitle().setText(article.getTitle());
        holder.getArticleDescription().setText(article.getDescription());
        if(article.getLocation() != null) {
            holder.getArticleLocationAddress().setText(article.getLocation().getAddress());
            holder.getArticleLocationLatLong().setText(article.getLocation().getLatLong());
        }
        if(article.getUser() != null) {
            holder.getArticleAuthor().setText("Por " + article.getUser().getUsername());
        }
        holder.getArticleShareButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) holder.itemView.getContext()).shareArticle(article, holder.getArticleImage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void updateData(List<Article> articles) {
        this.articles.clear();
        this.articles.addAll(articles);
        notifyDataSetChanged();
    }

    public void addArticle(Article article) {
        this.articles.add(article);
        notifyDataSetChanged();
    }

    public void addArticles(List<Article> articles) {
        this.articles.addAll(articles);
        notifyDataSetChanged();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {
        private ImageView articleImage;
        private TextView articleDocumentId;
        private TextView articleTitle;
        private TextView articleDescription;
        private TextView articleAuthor;
        private TextView articleLocationAddress;
        private TextView articleLocationLatLong;
        private FloatingActionButton articleShareButton;
        public ArticleViewHolder(View itemView) {
            super(itemView);
            articleDocumentId = itemView.findViewById(R.id.article_document_id);
            articleImage = itemView.findViewById(R.id.article_image);
            articleTitle = itemView.findViewById(R.id.article_title);
            articleDescription = itemView.findViewById(R.id.article_description);
            articleAuthor = itemView.findViewById(R.id.article_author);
            articleLocationAddress = itemView.findViewById(R.id.article_location_address);
            articleLocationLatLong = itemView.findViewById(R.id.article_location_latlong);
            articleShareButton = itemView.findViewById(R.id.article_share_button);
        }

        public TextView getArticleDocumentId() {
            return articleDocumentId;
        }

        public ImageView getArticleImage() {
            return articleImage;
        }

        public TextView getArticleDescription() {
            return articleDescription;
        }

        public TextView getArticleTitle() {
            return articleTitle;
        }

        public TextView getArticleAuthor() {
            return articleAuthor;
        }

        public TextView getArticleLocationAddress() {
            return articleLocationAddress;
        }

        public TextView getArticleLocationLatLong() {
            return articleLocationLatLong;
        }

        public FloatingActionButton getArticleShareButton() {
            return articleShareButton;
        }
    }
}
