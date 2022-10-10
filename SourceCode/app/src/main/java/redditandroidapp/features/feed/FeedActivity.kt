package redditandroidapp.features.feed

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.loading_badge.*
import redditandroidapp.R
import redditandroidapp.features.detailedview.DetailedViewFragment


// Main ('feed') view
@AndroidEntryPoint
class FeedActivity : AppCompatActivity() {

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postsListAdapter: PostsListAdapter
    private var isLoadingMoreItems: Boolean = false

    private val STATE_LOADING_ERROR = "STATE_LOADING_ERROR"
    private val STATE_CONTENT_LOADED = "STATE_CONTENT_LOADED"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputBartka(24)

        setupButtons()

//        app_title.setContent { MdcTheme { AppTitle() } }
//        loading_header.setContent { MdcTheme { LoadingHeader() } }
//        progressBar.setContent { MdcTheme { ProgressBar() } }
//        tryagain_button.setContent { MdcTheme { TryAgainButton(false) } }
//
//        // Todo: Fix the Try Again Button - it doesn't display!
//
//        // Initialize RecyclerView (feed items)
//        setupRecyclerView()
//
//        // Fetch feed items from the back-end and load them into the view
//        subscribeForFeedItems()
//
//        // Catch and handle potential update (e.g. network) issues
//        subscribeForFeedItemsorFeedItemscribeForUpdateError()
    }

    private fun setupButtons() {

        next_screen_button.setOnClickListener {

            val myIntent = Intent(this@FeedActivity, FeedActivityNextScreen::class.java)
            startActivity(myIntent);

        }


        red_button.setOnClickListener {

            printToDisplay("RED")

        }

        green_button.setOnClickListener {

            printToDisplay("GREEN")

        }

        RUDY_button.setOnClickListener {

            printToDisplay("RUDY")
        }
    }




    private fun inputBartka(temperature: Int) {
//        var pudelko_01: String = "PRZEMEK SZUMLINSKI"
//        val pudelko_02: String = "KAMIL CZOP"
//
//        pudelko_01 = "ZMIANKA 1"
////        pudelko_02 = "ZMIANKA 2"
//
//        printToDisplay(pudelko_01)
//        printToLog(pudelko_02)


        val LIMIT = 27
        val message1 = "TOO HOT"
        val message2 = "TOO COLD"

        if (temperature <= LIMIT) {
            printToDisplay(message2)
        } else {
            printToDisplay(message1)
        }



    }



    private fun printToDisplay(text: String) {
        text_display.setText(text)
    }

    private fun printToLog(text: String) {
        Log.d("BARTEK TAG", text)
    }

    @Composable
    private fun AppTitle() {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.h5
        )
    }

    @Composable
    private fun LoadingHeader() {
        Text(
            text = stringResource(R.string.loading),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 0.dp)
        )
    }

    @Composable
    private fun ProgressBar() {
        CircularProgressIndicator(modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 0.dp))
    }

    @Composable
    private fun TryAgainButton(isVisible: Boolean) {
        if (isVisible) {
            OutlinedButton(
                onClick = { tryAgainButtonClicked() },
                modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 0.dp)
            ) {
                Text(getString(R.string.try_again))
            }
        }
    }

    @Preview
    @Composable
    fun ComposablePreview() {
        AppTitle()
        LoadingHeader()
        CircularProgressIndicator()
        TryAgainButton(true)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
//        main_feed_recyclerview.layoutManager = layoutManager
//        postsListAdapter = PostsListAdapter(this) { postId: Int ->
//            displayDetailedView(postId)
//        }
//        main_feed_recyclerview.adapter = postsListAdapter
//        main_feed_recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//                if (layoutManager.findLastVisibleItemPosition() + 1 == postsListAdapter.itemCount) {
//                    loadMoreItems()
//                }
//            }
//        })
    }

    private fun loadMoreItems() {
        if (!isLoadingMoreItems) {
            isLoadingMoreItems = true
            val lastPostId = postsListAdapter.getLastPostId()
            lastPostId?.let {
                viewModel.fetchMorePosts(it)
            }
        }
    }

    private fun subscribeForFeedItems() {
        viewModel.subscribeForPosts(true)?.observe(this) {

            if (!it.isNullOrEmpty()) {
                setViewState(STATE_CONTENT_LOADED)

                // Display fetched items
                postsListAdapter.setPosts(it)
            }

            isLoadingMoreItems = false
        }
    }

    private fun subscribeForUpdateError() {
        viewModel.subscribeForUpdateErrors()?.observe(this) {

            // Case of Network Error if no items have been cached
            if (postsListAdapter.itemCount == 0) {
                setViewState(STATE_LOADING_ERROR)
            }

            // Display error message to the user
            Toast.makeText(
                this,
                R.string.network_problem_check_internet_connection,
                Toast.LENGTH_LONG
            ).show()

            isLoadingMoreItems = false
        }
    }

    private fun refreshPostsSubscription() {
        viewModel.refreshPosts()
    }

    private fun displayDetailedView(postId: Int) {
        val fragment = DetailedViewFragment()
        val bundle = Bundle()
        bundle.putInt("postId", postId)
        fragment.arguments = bundle

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setViewState(state: String) {
        when(state) {
            STATE_LOADING_ERROR -> setupLoadingErrorView()
            STATE_CONTENT_LOADED -> setupContentLoadedView()
        }
    }

    private fun setupLoadingErrorView() {
        // Stop the loading progress bar (circle)
        progressBar.visibility = View.INVISIBLE

        // Display "Try Again" button
        tryagain_button.isVisible = true
//        tryagain_button.visibility = View.VISIBLE
    }

    private fun tryAgainButtonClicked() {
        // Reset the feed data subscription
        refreshPostsSubscription()

        // Re-display the loading progress bar (circle)
        progressBar.visibility = View.VISIBLE
    }

    private fun setupContentLoadedView() {
        // Hide the loading view
        loading_container.visibility = View.GONE
//        appbar_container.visibility = View.VISIBLE

        // Setup refresh button
        btn_refresh.setOnClickListener{
            refreshPostsSubscription()
        }
    }
}
