package com.enzo.architecture.view_viemodel.gallery

import android.app.SearchManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.enzo.architecture.R
import com.enzo.architecture.databinding.FragmentGalleryBinding
import com.enzo.architecture.model.UnsplashPhoto
import com.enzo.architecture.util.MySearchSuggestionsProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery),
    UnsplashPhotoAdapter.OnItemClickListener {
    private val viewModel by viewModels<GalleryViewModel>()
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    lateinit var mSuggestionAdapter:HistorySearchAdapter
    lateinit var suggestions:SearchRecentSuggestions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGalleryBinding.bind(view)
        val adapter = UnsplashPhotoAdapter(this)

        //對象實例初始化時需要對對像中的屬性進行賦值 & 返回該對象
        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadStateAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadStateAdapter { adapter.retry() },
            )
            buttonRetry.setOnClickListener { adapter.retry() }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                buttonRetry.isVisible = loadState.source.refresh is LoadState.Error
                textViewError.isVisible = loadState.source.refresh is LoadState.Error

                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true
                } else {
                    textViewEmpty.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_gallery, menu)
        mSuggestionAdapter = activity?.let { HistorySearchAdapter(it, R.layout.item_search_history_list, null) }!!

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.suggestionsAdapter = mSuggestionAdapter
        suggestions = SearchRecentSuggestions(activity,
                MySearchSuggestionsProvider.AUTHORITY, MySearchSuggestionsProvider.MODE)



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {
                    suggestions!!.saveRecentQuery(query, null)
                    binding.recyclerView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val cursor = getRecentSuggestions(newText!!)
                mSuggestionAdapter!!.swapCursor(cursor)
                return true
            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val str = mSuggestionAdapter!!.getSuggestionText(position)
                searchView.setQuery(str, true)
                if (str != null) {
                    viewModel.searchPhotos(str)
                }
                return true
            }

        })

    }

    override fun onItemClick(photo: UnsplashPhoto) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(photo)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * 查找歷史搜尋紀錄
     */
    fun getRecentSuggestions(query: String): Cursor? {
        val uriBuilder = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(MySearchSuggestionsProvider.AUTHORITY)

        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)

        val selection = " ?"
        val selArgs = arrayOf(query)

        val uri = uriBuilder.build()

        return activity?.contentResolver?.query(uri, null, selection, selArgs, null)
    }

    }