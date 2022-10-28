package dev.bartuzen.qbitcontroller.ui.torrent.tabs.trackers

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.bartuzen.qbitcontroller.R
import dev.bartuzen.qbitcontroller.databinding.DialogTorrentTrackersAddBinding
import dev.bartuzen.qbitcontroller.databinding.FragmentTorrentTrackersBinding
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.ui.base.ArgsFragment
import dev.bartuzen.qbitcontroller.utils.getErrorMessage
import dev.bartuzen.qbitcontroller.utils.launchAndCollectIn
import dev.bartuzen.qbitcontroller.utils.launchAndCollectLatestIn
import dev.bartuzen.qbitcontroller.utils.setItemMargin
import dev.bartuzen.qbitcontroller.utils.showSnackbar

@FragmentWithArgs
@AndroidEntryPoint
class TorrentTrackersFragment : ArgsFragment(R.layout.fragment_torrent_trackers) {
    private var _binding: FragmentTorrentTrackersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TorrentTrackersViewModel by viewModels()

    @Arg
    lateinit var serverConfig: ServerConfig

    @Arg
    lateinit var torrentHash: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTorrentTrackersBinding.bind(view)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.torrent_trackers_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_add -> {
                        showAddTrackersDialog()
                    }
                    else -> return false
                }

                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        var actionMode: ActionMode? = null
        val adapter = TorrentTrackersAdapter().apply {
            onSelectionModeStart {
                actionMode = requireActivity().startActionMode(object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        mode.menuInflater.inflate(R.menu.torrent_trackers_selection_menu, menu)
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
                        when (item.itemId) {
                            R.id.menu_delete -> {
                                showDeleteTrackersDialog(this@apply, actionMode)
                                true
                            }
                            R.id.menu_select_all -> {
                                selectAll()
                                true
                            }
                            R.id.menu_select_inverse -> {
                                selectInverse()
                                true
                            }
                            else -> false
                        }

                    override fun onDestroyActionMode(mode: ActionMode) {
                        finishSelection()
                    }
                })
            }
            onSelectionModeEnd {
                actionMode?.finish()
            }
            onUpdateSelection {
                val itemCount = selectedItemCount
                if (itemCount != 0) {
                    actionMode?.title = "$itemCount trackers selected"
                }
            }
        }
        binding.recyclerTrackers.adapter = adapter
        binding.recyclerTrackers.setItemMargin(8, 8)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshTrackers(serverConfig, torrentHash)
        }

        if (!viewModel.isInitialLoadStarted) {
            viewModel.isInitialLoadStarted = true
            viewModel.loadTrackers(serverConfig, torrentHash)
        }

        viewModel.isLoading.launchAndCollectLatestIn(viewLifecycleOwner) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isRefreshing.launchAndCollectLatestIn(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }

        viewModel.torrentTrackers.launchAndCollectLatestIn(viewLifecycleOwner) { trackers ->
            adapter.submitList(trackers)
        }

        viewModel.eventFlow.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is TorrentTrackersViewModel.Event.Error -> {
                    showSnackbar(getErrorMessage(requireContext(), event.error))
                }
                TorrentTrackersViewModel.Event.TrackersAdded -> {
                    showSnackbar(getString(R.string.torrent_trackers_added))

                    viewModel.loadTrackers(serverConfig, torrentHash)
                }
                TorrentTrackersViewModel.Event.TrackersDeleted -> {
                    showSnackbar(getString(R.string.torrent_trackers_deleted))

                    viewModel.loadTrackers(serverConfig, torrentHash)
                }
            }
        }
    }

    private fun showAddTrackersDialog() {
        val dialogBinding = DialogTorrentTrackersAddBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.torrent_trackers_add)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.addTrackers(
                    serverConfig,
                    torrentHash,
                    dialogBinding.editTrackers.text.toString()
                )
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    private fun showDeleteTrackersDialog(adapter: TorrentTrackersAdapter, actionMode: ActionMode?) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(
                resources.getQuantityString(
                    R.plurals.torrent_trackers_delete,
                    adapter.selectedItemCount,
                    adapter.selectedItemCount
                )
            )
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.deleteTrackers(
                    serverConfig,
                    torrentHash,
                    adapter.selectedItems.toList()
                )
                adapter.finishSelection()
                actionMode?.finish()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}