package dev.bartuzen.qbitcontroller.ui.rss.articles

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.bartuzen.qbitcontroller.R
import dev.bartuzen.qbitcontroller.databinding.FragmentRssArticlesBinding
import dev.bartuzen.qbitcontroller.model.Article
import dev.bartuzen.qbitcontroller.ui.addtorrent.AddTorrentActivity
import dev.bartuzen.qbitcontroller.utils.getErrorMessage
import dev.bartuzen.qbitcontroller.utils.launchAndCollectIn
import dev.bartuzen.qbitcontroller.utils.launchAndCollectLatestIn
import dev.bartuzen.qbitcontroller.utils.requireAppCompatActivity
import dev.bartuzen.qbitcontroller.utils.showDialog
import dev.bartuzen.qbitcontroller.utils.showSnackbar
import dev.bartuzen.qbitcontroller.utils.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RssArticlesFragment() : Fragment(R.layout.fragment_rss_articles) {
    private val binding by viewBinding(FragmentRssArticlesBinding::bind)

    private val viewModel: RssArticlesViewModel by viewModels()

    private val serverId get() = arguments?.getInt("serverId", -1).takeIf { it != -1 }!!
    private val feedPath get() = arguments?.getStringArrayList("feedPath")!!

    private val startAddTorrentActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isAdded = result.data?.getBooleanExtra(
                    AddTorrentActivity.Extras.IS_ADDED,
                    false
                ) ?: false
                if (isAdded) {
                    showSnackbar(R.string.torrent_add_success)
                }
            }
        }

    constructor(serverId: Int, feedPath: List<String>) : this() {
        arguments = bundleOf(
            "serverId" to serverId,
            "feedPath" to ArrayList(feedPath)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireAppCompatActivity().supportActionBar?.title = feedPath.lastOrNull() ?: getString(R.string.rss_all_articles)

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.rss_articles, menu)

                    val searchItem = menu.findItem(R.id.menu_search)

                    val searchView = searchItem.actionView as SearchView
                    searchView.queryHint = getString(R.string.rss_filter)
                    searchView.isSubmitButtonEnabled = false
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?) = false

                        override fun onQueryTextChange(newText: String?): Boolean {
                            viewModel.setSearchQuery(newText ?: "")
                            return true
                        }
                    })

                    searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                            for (menuItem in menu.iterator()) {
                                menuItem.isVisible = false
                            }

                            searchView.maxWidth = Integer.MAX_VALUE
                            return true
                        }

                        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                            requireActivity().invalidateOptionsMenu()
                            return true
                        }
                    })
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.menu_mark_all_as_read -> {
                            viewModel.markAsRead(serverId, feedPath, null)
                        }
                        R.id.menu_refresh -> {
                            viewModel.refreshFeed(serverId, feedPath)
                        }
                        else -> return false
                    }
                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        if (!viewModel.isInitialLoadStarted) {
            viewModel.isInitialLoadStarted = true
            viewModel.loadRssArticles(serverId, feedPath)
        }

        val adapter = RssArticlesAdapter(
            onClick = { article ->
                showArticleDialog(
                    article = article,
                    onDownload = {
                        val intent = Intent(requireActivity(), AddTorrentActivity::class.java).apply {
                            putExtra(AddTorrentActivity.Extras.SERVER_ID, serverId)
                            putExtra(AddTorrentActivity.Extras.TORRENT_URL, article.torrentUrl)
                        }
                        startAddTorrentActivity.launch(intent)
                    },
                    onMarkAsRead = {
                        viewModel.markAsRead(serverId, feedPath, article.id)
                    }
                )
            }
        )
        binding.recyclerArticles.adapter = adapter
        binding.recyclerArticles.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val verticalPx = 8.toPx(requireContext())
                val horizontalPx = 8.toPx(requireContext())
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = verticalPx
                }
                outRect.bottom = verticalPx
                outRect.left = horizontalPx
                outRect.right = horizontalPx
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshRssArticles(serverId, feedPath)
        }

        viewModel.isLoading.launchAndCollectLatestIn(viewLifecycleOwner) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isRefreshing.launchAndCollectLatestIn(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }

        viewModel.filteredArticles.launchAndCollectLatestIn(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)
        }

        viewModel.eventFlow.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is RssArticlesViewModel.Event.Error -> {
                    showSnackbar(getErrorMessage(requireContext(), event.error))
                }
                RssArticlesViewModel.Event.RssFeedNotFound -> {
                    showSnackbar(R.string.rss_feed_not_found)
                }
                RssArticlesViewModel.Event.ArticleMarkedAsRead -> {
                    showSnackbar(R.string.rss_mark_article_as_read_success)
                    viewModel.loadRssArticles(serverId, feedPath)
                }
                RssArticlesViewModel.Event.AllArticlesMarkedAsRead -> {
                    showSnackbar(R.string.rss_mark_all_articles_as_read_success)
                    viewModel.loadRssArticles(serverId, feedPath)
                }
                RssArticlesViewModel.Event.FeedRefreshed -> {
                    showSnackbar(R.string.rss_refresh_feed_success)

                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(1000)
                        viewModel.loadRssArticles(serverId, feedPath)
                    }
                }
            }
        }
    }

    private fun showArticleDialog(article: Article, onDownload: () -> Unit, onMarkAsRead: () -> Unit) {
        showDialog {
            setTitle(article.title)

            val descriptionText = article.description ?: "<i>${context.getString(R.string.rss_no_description)}</i>"

            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(descriptionText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(descriptionText)
            }
            setMessage(description)

            setPositiveButton(R.string.rss_download) { _, _ ->
                onDownload()
            }
            setNeutralButton(R.string.rss_mark_as_read) { _, _ ->
                onMarkAsRead()
            }
        }
    }
}
