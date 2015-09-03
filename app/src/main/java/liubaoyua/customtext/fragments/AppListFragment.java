package liubaoyua.customtext.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.AppRecyclerAdapter;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.NewListEvent;
import liubaoyua.customtext.ui.SetTextActivity;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;

public class AppListFragment extends Fragment {

    private static int count = 0;
    private RecyclerView mRecyclerView;
    private AppRecyclerAdapter appRecyclerAdapter;
    private Communicator communicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment_app, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        return mRecyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext(), LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        appRecyclerAdapter = new AppRecyclerAdapter(getActivity(), new ArrayList<AppInfo>());
        appRecyclerAdapter = new AppRecyclerAdapter(getActivity(), new ArrayList<AppInfo>());
        mRecyclerView.setAdapter(appRecyclerAdapter);
        appRecyclerAdapter.setOnItemClickListener(new AppRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String packageName) {
                Intent intent = new Intent(getActivity(), SetTextActivity.class);
                intent.putExtra(Common.POSITION_ARG, position);
                intent.putExtra(Common.PACKAGE_NAME_ARG, packageName);
                getActivity().startActivityForResult(intent, Common.APP_REQUEST_CODE);
            }
        });
        // 两个界面都加载好后进行数据加载
        count ++;

    }

    public void filter(String nameFilter){
        if(appRecyclerAdapter != null)
           appRecyclerAdapter.getFilter().filter(nameFilter);
    }

    public List<AppInfo> getAppList() {
        if (appRecyclerAdapter != null) {
            return appRecyclerAdapter.getFilter().getAppList();
        } else {
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public void setAppList(List<AppInfo> appList) {
        if(appRecyclerAdapter != null){
            appRecyclerAdapter.getFilter().setAppList(appList);
        }else{
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public List<AppInfo> getShowingAppList(){
        if(appRecyclerAdapter != null){
            return appRecyclerAdapter.getAppList();
        }else{
            throw new IllegalStateException("appRecyclerAdapter is null");
        }
    }

    public void notifyDataSetChanged(){
        if(appRecyclerAdapter != null)
            appRecyclerAdapter.notifyDataSetChanged();
    }

    public void stopScrolling() {
        if (mRecyclerView != null) {
            mRecyclerView.stopScroll();
        }
    }

    public void scrollToTopOrBottom() {
        if (mRecyclerView == null) {
            Utils.myLog("in method scrollToTopOrBottom :  mRecyclerView is null");
            return;
        }


        if (mRecyclerView.canScrollVertically(-1)) {
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            mRecyclerView.smoothScrollToPosition(appRecyclerAdapter.getItemCount() - 1);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.myLog("onStop" + "   " + (appRecyclerAdapter == null));
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.myLog("onPause" + "   " + (appRecyclerAdapter == null));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (count >= 2) {
            EventBus.getDefault().post(new NewListEvent());
            count = 0;
        }
        Utils.myLog("onResume" + "   " + (appRecyclerAdapter == null));
    }


    @Override
    public void onStart() {
        super.onStart();
        Utils.myLog("onStart" + "   " + (appRecyclerAdapter == null));
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Utils.myLog("onDetach" + "   " + (appRecyclerAdapter == null));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.myLog("onDestroy" + "   " + (appRecyclerAdapter == null));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        communicator = (Communicator)context;
        Utils.myLog("onAttach" + "   " + (appRecyclerAdapter == null));
    }

    public interface Communicator {
        List<AppInfo> getList();
    }
}
