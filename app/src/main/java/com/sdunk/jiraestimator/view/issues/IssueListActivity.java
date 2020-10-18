package com.sdunk.jiraestimator.view.issues;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.adapters.GenericRVAdapter;
import com.sdunk.jiraestimator.api.APIUtils;
import com.sdunk.jiraestimator.api.JiraService;
import com.sdunk.jiraestimator.databinding.ActivityIssueListBinding;
import com.sdunk.jiraestimator.databinding.IssueListBinding;
import com.sdunk.jiraestimator.databinding.IssueListContentBinding;
import com.sdunk.jiraestimator.db.issue.IssueDatabase;
import com.sdunk.jiraestimator.model.GenericResponse;
import com.sdunk.jiraestimator.model.JiraIssue;
import com.sdunk.jiraestimator.view.issues.dummy.DummyContent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Issues. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link IssueDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class IssueListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ArrayList<JiraIssue> issues = new ArrayList<>();

    private ActivityIssueListBinding binding;
    private IssueListBinding listBinding;

    private JiraService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APIUtils.updateIssueCache(getApplicationContext());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_issue_list);
        listBinding = DataBindingUtil.getBinding(binding.issueListLayout.issueList);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(getTitle());
        listBinding.issueList.setLayoutManager(new LinearLayoutManager(this));
        listBinding.issueList.setAdapter(new GenericRVAdapter<JiraIssue, IssueListContentBinding>(this, issues) {
            @Override
            public int getLayoutResId() {
                return R.layout.issue_list_item;
            }

            @Override
            public void onBindData(JiraIssue issue, int position, IssueListContentBinding dataBinding) {
                dataBinding.setIssue(issue);
            }

            @Override
            public void onItemClick(JiraIssue issue, int position) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(IssueDetailFragment.ARG_ITEM_ID, issue);
                    IssueDetailFragment fragment = new IssueDetailFragment();
                    fragment.setArguments(arguments);
                    IssueListActivity.this.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.issue_detail_container, fragment)
                            .commit();
                } else {
                    Context context = IssueListActivity.this.getApplicationContext();
                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra(IssueDetailFragment.ARG_ITEM_ID, issue);

                    context.startActivity(intent);
                }
            }
        });


        if (findViewById(R.id.issue_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final IssueListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();

            }
        };

        SimpleItemRecyclerViewAdapter(IssueListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.issue_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}