package com.vallny.bzdzk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vallny.bzdzk.adapter.TreeAdapter;
import com.vallny.bzdzk.bean.TreeBean;
import com.vallny.bzdzk.util.TreeHelper;
import com.vallny.bzdzk.util.JSONHelper;
import com.vallny.bzdzk.util.URLHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class TreeFragment extends SherlockFragment implements OnRefreshListener<ListView>, OnItemClickListener, OnItemLongClickListener {

	private final static String TAG = "com.vallny.bzdzk";
	private View view;
	private PullToRefreshListView mPullRefreshListView;
	private BzdzkActivity activity;

	// private Context context;
	private TreeAdapter treeAdapter;

	private int pageNo = 1;

	private ArrayList<TreeBean> itemList = new ArrayList<TreeBean>();
	private ArrayList<TreeBean> list;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	private Boolean isOnline;

	private String url;

	private TreeBean parent_tree;
	private TreeBean curent_tree;

	public TreeFragment() {
	}

	public TreeFragment(String url, ArrayList<TreeBean> list) {
		this.list = list;
		this.url = url;
	}

	public TreeFragment(ArrayList<TreeBean> list) {
		this.list = list;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (view == null) {
			view = inflater.inflate(R.layout.tree_list, null);

			mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
			treeAdapter = new TreeAdapter(activity);
			treeAdapter.addItemList(list);
			mPullRefreshListView.setAdapter(treeAdapter);
			// mPullRefreshListView.setRefreshing();
			mPullRefreshListView.setOnRefreshListener(this);
			mPullRefreshListView.getRefreshableView().setOnItemClickListener(this);
			mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(this);
			mPullRefreshListView.getRefreshableView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			// pageNo = 1;

			// new AsyncTreeTask(pageNo, false).execute(url);

		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (BzdzkActivity) activity;
	}

	/**
	 * 点击
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.e(TAG, position + "****" + id + "Click");
		mPullRefreshListView.getRefreshableView().setItemChecked(position, true);

		TreeBean tree = (TreeBean) parent.getAdapter().getItem(position);
		setCurent_tree(tree);
		if (canMark(tree)) {
			String layer = tree.getId().split(",")[0];
			activity.updateLayer(layer, tree, view);
		}
	}

	private boolean canMark(TreeBean tree) {
		String type = tree.getId().split(",")[0];
		Log.e(TAG, type);
		return !(type.equals("zrq") || type.equals("jlx") || type.equals("zrc"));
	}

	/**
	 * 长按
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {

		Log.e(TAG, position + "****" + id + "LongClick");
		activity.hiddenButton();
		activity.showProgressBar(false);
		activity.setShowGraphic(false);
		TreeBean tree = (TreeBean) arg0.getAdapter().getItem(position);
		String url = URLHelper.TREE + "?sjid=" + tree.getId();
		TreeHelper.getInstance(activity, false, tree).initTree(url);

		return true;
	}

	public TreeBean getParent_tree() {
		return parent_tree;
	}

	public void setParent_tree(TreeBean parent_tree) {
		this.parent_tree = parent_tree;
	}

	public TreeBean getCurent_tree() {
		return curent_tree;
	}

	public void setCurent_tree(TreeBean curent_tree) {
		this.curent_tree = curent_tree;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		pageNo++;

		new AsyncTreeTask(pageNo).execute(url);

	}

	class AsyncTreeTask extends AsyncTask<String, Void, Void> {

		private long pageNo;
		private boolean isNew;
		private ArrayList<TreeBean> newList;

		public AsyncTreeTask(long pageNo) {
			this.pageNo = pageNo;
		}

		@Override
		protected Void doInBackground(String... params) {
			isOnline = true;
			String json = URLHelper.queryStringForGet(params[0] + "&pageNo=" + pageNo);
			if (json == null || json.equals("连接错误！")) {
				isOnline = false;
			} else if ("".equals(json)) {
				return null;
			} else {
				newList = (ArrayList<TreeBean>) JSONHelper.JSON2List(json);
				// itemList.addAll(newList);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (newList == null) {
				Toast.makeText(activity, R.string.none, Toast.LENGTH_LONG).show();
			} else {
				if (isOnline) {
					treeAdapter.addItemList(itemList);
					treeAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(activity, R.string.online_error, Toast.LENGTH_LONG).show();
				}
			}
			mPullRefreshListView.onRefreshComplete();

		}

	}


}
