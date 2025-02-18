package gtg.virus.gtpr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import gtg.virus.gtpr.aaentity.AABook;
import gtg.virus.gtpr.adapters.PagerAdapter;
import gtg.virus.gtpr.entities.PBook;
import gtg.virus.gtpr.fragment_pages.WebPageFragment;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

import static gtg.virus.gtpr.utils.Utilities.PIN_EXTRA_PBOOK;

public class GTGEpubViewer extends AbstractViewer implements AbstractViewer.OnActionBarItemClick {

    private final static String TAG = GTGEpubViewer.class.getSimpleName();

    @InjectView(R.id.pager)
    protected ViewPager mPager;

    private PagerAdapter mAdapter;

    private List<Fragment> mFragments;



    Book epubBook = null;

    /**
     * @return the layout from R.
     */
    @Override
    protected int getContentViewResId() {
        return R.layout.epub_main_layout;
    }

    @Override
    protected void initializeResources(Bundle saveInstanceState) {


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String gsonExtra =  extras.getString(PIN_EXTRA_PBOOK);

            mBook = new Gson().fromJson(gsonExtra, PBook.class);

            if(extras.containsKey(INDEX_KEY)){
                index_key = extras.getInt(INDEX_KEY);
            }
        }

        current_book = AABook.find(mBook.getPath());


        EpubReader epubReader = new EpubReader();
        epubBook = null;
        try {
            epubBook = epubReader.readEpub(new FileInputStream(mBook.getPath()));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mFragments = new ArrayList<Fragment>();
/*        for(int i= 0 ; i < epubBook.getContents().size() ; i++){
            try {
                String data = new String(epubBook.getContents().get(i).getData());
                mFragments.add(WebPageFragment.newInstance(data , mBook.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/
/*        List<TOCReference> mref = epubBook.getTableOfContents().getTocReferences();
        Log.w(TAG , " Size of TocReferece " + mref.size() );
        for(TOCReference ref : mref){
            try {
                String data = new String(ref.getResource().getData());
                mFragments.add(WebPageFragment.newInstance(data , mBook.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/

        logContentsTable(epubBook.getTableOfContents().getTocReferences() , 0);

        Log.w(TAG , "Fragments size " + mFragments.size());
        mAdapter = new PagerAdapter(getSupportFragmentManager() , mFragments);

        //mPager.setPageTransformer(true , new FlipHorizontalTransformer());
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

                Log.w(TAG, "onPageScrolled " + i + " " + i2);
            }

            @Override
            public void onPageSelected(int i) {
                setCurrentPage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.w(TAG, "onPageScrollStateChanged");
            }
        });

        mPager.setAdapter(mAdapter);

        if(index_key >= 0){
            mPager.setCurrentItem(index_key , true);
            setCurrentPage(index_key);
        }else{
            setCurrentPage(0);
        }

        maxPage = mAdapter.getCount();

/*        if(current_book != null){
            if(current_book.status == 0 && current_book.status != 1){
                current_book.status = 1;
                current_book.save();
            }else if(current_book.status == 2 ){
                current_book.status = 3;
                current_book.save();
            }
        }*/

    }

    @Override
    protected void changeViewItem(MenuItem item) {
        item.setTitle("View Epub Details");
    }

    private void logContentsTable(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }
        int page = 0;
        String charEncoding = null;
        for (TOCReference tocReference:tocReferences) {
            StringBuilder tocString = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                tocString.append("\t");
            }
            tocString.append(tocReference.getTitle());

/*            row.setTitle(tocString.toString());
            row.setResource(tocReference.getResource());
            contentDetails.add(row);*/
            try {


            /*    final String stringData = IOUtils.toString(tocReference.getResource().getInputStream(), tocReference.getResource().getInputEncoding());
                Log.w(TAG , "Data String=> " + stringData );
                */
                charEncoding = tocReference.getResource().getInputEncoding();
                mFragments.add(WebPageFragment.newInstance(new String(tocReference.getResource().getData()) , mBook.getPath() , page , charEncoding));
                Log.w(TAG , "Page = " + page);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logContentsTable(tocReference.getChildren(), depth + 1);
            page++;
        }
    }

    @Override
    public void onItemClick(MenuItem item) {

    }

    @Override
    public void onItemClick(int resId) {

    }

    @Override
    public void onSearch() {

    }

    @Override
    public void onSetAlarm() {

    }

    @Override
    public void onViewDetails() {

    }
}
