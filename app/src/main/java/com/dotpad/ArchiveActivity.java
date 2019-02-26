package com.dotpad;

import com.dotpad.db.DotManager;
import com.dotpad.db.DotManager.Contents;
import com.dotpad.logic.adapters.ArchiveAdapter;
import com.dotpad.model.Dot;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import java.util.List;

public class ArchiveActivity 
	extends ListActivity 
	implements OnItemLongClickListener, OnScrollListener {

	private TextView mSearch;
	private View mLoadAll;
	private View mLoadingView;
	private ArchiveAdapter mAdapter;
	private DotManager mManager;
	private Contents mContentsType;

    private int mVisibleThreshold = 20;
    private int mCurrentPage = 0;
    private int mPreviousTotal = 0;
    private int mTotalPages = 0;
    private boolean mLoading = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.activity_archive);

		boolean showOnlyActiveDots = this.getIntent().hasExtra(this.getString(R.string.active_key));
		this.mContentsType = showOnlyActiveDots ? Contents.ACTIVE : Contents.ARCHIVAL;

		if (showOnlyActiveDots) {
			mVisibleThreshold = 100;
		}

		this.mManager = new DotManager(this);
		this.mAdapter = new ArchiveAdapter(this, 
				R.layout.archive_row,
				this.mManager.dots(this.mContentsType, this.mCurrentPage,
						this.mVisibleThreshold, mContentsType == Contents.ACTIVE));
		
		this.mTotalPages = (int)Math.ceil(
				(double)this.mManager.count(this.mContentsType) / (double)this.mVisibleThreshold);
		
		this.setListAdapter(this.mAdapter);
		this.getListView().setOnItemLongClickListener(this);
		
		this.getListView().setOnScrollListener(this);

		this.mLoadAll = this.findViewById(R.id.archive_load_all);
		this.mLoadingView = this.findViewById(R.id.archive_loading);
		this.mSearch = (TextView)this.findViewById(R.id.archive_search);

		this.mLoadAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadAll();
			}
		});

		this.mSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mAdapter.getFilter().filter(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapterView, View view,
			final int position, long id) {

		new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_delete_title)
			.setPositiveButton(R.string.dialog_delete_delete, new OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArchiveActivity.this.deleteDot((Dot)adapterView.getItemAtPosition(position));
				}
			})
			.setNeutralButton(R.string.dialog_delete_restore, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArchiveActivity.this.restoreDot((Dot)adapterView.getItemAtPosition(position));					
				}
			})
			.setNegativeButton(R.string.dialog_delete_cancel, null)
			.show();
		
		return false;
	}
	
	private void deleteDot(Dot dot) {

		this.mManager.delete(dot.id);
		this.refresh();
	}
	
	private void restoreDot(Dot dot) {

		this.mManager.setIsArchival(dot, false);
		this.refresh();
	}
	
	private void refresh() {

		this.mAdapter.clear();
		this.mAdapter.addAll(this.mManager.dots(this.mContentsType));
		this.mAdapter.notifyDataSetChanged();
	}

	private void loadAll() {
		this.mLoadingView.setVisibility(View.VISIBLE);

		(new AsyncTask<Void, Void, List<Dot>>() {
			@Override
			protected List<Dot> doInBackground(Void... params) {
				return mManager.dots(Contents.ARCHIVAL);
			}

			@Override
			protected void onPostExecute(List<Dot> dots) {
				mAdapter.clear();
				mAdapter.addAll(dots);
				mAdapter.notifyDataSetChanged();

				mLoadingView.setVisibility(View.GONE);
			}
		}).execute();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (this.mCurrentPage >= this.mTotalPages)
			return;
		
		if (this.mLoading && totalItemCount > this.mPreviousTotal) {
				
			this.mLoading = false;
			this.mPreviousTotal = totalItemCount;
			this.mCurrentPage++;
		}
		else if (!this.mLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + this.mVisibleThreshold)) {

			this.mAdapter.addAll(this.mManager.dots(this.mContentsType,
					this.mCurrentPage * this.mVisibleThreshold, this.mVisibleThreshold));
			this.mAdapter.notifyDataSetChanged();

			this.mLoading = true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (this.mSearch != null)
			this.mSearch.addTextChangedListener(null);

		if (this.mLoadAll != null)
			this.mLoadAll.setOnClickListener(null);
	}
}