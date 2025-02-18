package gtg.virus.gtpr;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.InjectView;
import gtg.virus.gtpr.aaentity.AARemoteBook;
import gtg.virus.gtpr.adapters.RemoteShelfAdapter;
import gtg.virus.gtpr.base.BaseFragment;
import gtg.virus.gtpr.entities.RemoteBook;
import gtg.virus.gtpr.entities.User;
import gtg.virus.gtpr.retrofit.Constants;
import gtg.virus.gtpr.retrofit.EbookQueryService;
import gtg.virus.gtpr.retrofit.RemoteQueryEbook;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by DavidLuvelleJoseph on 2/16/2015.
 */
public class MyRemoteBookShelf extends BaseFragment implements RemoteShelfAdapter.OnViewClick {

    private static final String TAG = MyRemoteBookShelf.class.getSimpleName();
    @InjectView(R.id.shelf_list_view)
    protected ListView mListView;

    private RemoteShelfAdapter mShelfAdapter;

    @Override
    protected boolean hasOptions() {
        return false;
    }

    @Override
    protected void provideOnCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mShelfAdapter = new RemoteShelfAdapter(getActivity());
        mListView.setAdapter(mShelfAdapter);
        mShelfAdapter.setmClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER)
                .build();


        User user = User.getUser();

        final ProgressDialog dialog  = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();
        EbookQueryService service = adapter.create(EbookQueryService.class);
        service.queryEbooks(user.remote_id , new Callback<RemoteQueryEbook>() {
            @Override
            public void success(RemoteQueryEbook remotePBooks, Response response) {
                dialog.dismiss();


                if(!isAdded() || !isVisible()){
                    return;
                }

                if(remotePBooks.getStatus().equals(getString(R.string.success))){
                    if(remotePBooks != null && remotePBooks.getEntity() != null) {
                        List<RemoteBook> ebooks = remotePBooks.getEntity().getEbook();
                        if (ebooks != null && !ebooks.isEmpty()) {
                            for (int i = 0; i < ebooks.size(); i++) {
                                //RemotePBook pbook = new RemotePBook();
                                Log.w(TAG, ebooks.get(i).getPath());
                                RemoteBook remoteBook = ebooks.get(i);
                                AARemoteBook aaRemoteBook = new AARemoteBook();
                                aaRemoteBook.fileName = remoteBook.getFilename();
                                aaRemoteBook.author = remoteBook.getAuthor();
                                aaRemoteBook.format = remoteBook.getFormat();
                                aaRemoteBook.path = remoteBook.getPath();
                                aaRemoteBook.remoteDivisionId = remoteBook.getDivision_id();
                                aaRemoteBook.remoteId = remoteBook.getId();
                                aaRemoteBook.remoteUserId = remoteBook.getUser_id();
                                aaRemoteBook.status = remoteBook.getStatus();
                                aaRemoteBook.title = remoteBook.getTitle();
                                long id = aaRemoteBook.save();
                                if (id > 0) {
                                    mShelfAdapter.addBook(aaRemoteBook);
                                }
                            }
                        }
                    }

                }else{
                    AlertDialog alert = new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.retry))
                            .setMessage(remotePBooks.getMessage())
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            }).create();

                    alert.show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                dialog.dismiss();

                if(!isAdded() || !isVisible()){
                    return;
                }
                AlertDialog alert = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.retry))
                        .setMessage(getString(R.string.provide_correct_credentials))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        }).create();

                alert.show();
            }
        });

    }

    @Override
    protected int resourceId() {
        return R.layout.shelf_fragment;
    }



    @Override
    public void bookClick(AARemoteBook book, int position) {
        final String url = Constants.SERVER + Constants.EBOOK_QUERY_SUBDOMAIN + book.path;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Grabbing file from repo");
        request.setTitle("Grabbing...");
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "pinbook-downloads");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
