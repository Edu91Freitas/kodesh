package na.komi.kodesh.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import na.komi.kodesh.R
import na.komi.kodesh.model.Bible
import na.komi.kodesh.ui.internal.BaseKatanaFragment
import na.komi.kodesh.ui.internal.BottomSheetBehavior2
import na.komi.kodesh.ui.internal.FragmentToolbar
import na.komi.kodesh.ui.main.MainViewModel
import na.komi.kodesh.util.closestKatana
import na.komi.kodesh.util.viewModel
import org.rewedigital.katana.Component

class SearchFragment : BaseKatanaFragment() {
    override val layout: Int = R.layout.fragment_search

    override val component: Component by closestKatana()

    private val viewModel: MainViewModel by viewModel()

    override fun ToolbarBuilder(): FragmentToolbar {
        return FragmentToolbar(
                toolbar = R.id.toolbar_main,
                title = R.string.kod_search_title,
                bottomSheet = R.id.main_bottom_container,
                navigationView = R.id.navigation_view
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getToolbar()?.let {
            for (a in it.menu.children)
                if (a.itemId == R.id.styling)
                    a.isVisible = false
        }
        getNavigationView().let {
            it.menu.findItem(R.id.action_find_in_page).isEnabled = false
            it.setCheckedItem(R.id.action_search)
        }
        setupRecyclerView()
        val bh = getBottomSheetBehavior()
        launch(Dispatchers.Main) {
            while (bh?.state != BottomSheetBehavior2.STATE_COLLAPSED) delay(10)
            val editText = view?.text_input_edit_text
            editText?.requestFocus()
        }
    }

    fun setupRecyclerView() {
        val rv: RecyclerView = view!!.recycler_view_search
        val adapter = SearchAdapter()
        val editText: TextInputEditText = view!!.text_input_edit_text as TextInputEditText
        var job = Job()
        val SEARCH_DEBOUNCE_MS = 300.toLong()

        rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        val searchVerseObserver = Observer<PagedList<Bible>> { list ->
            list?.let {
                adapter.submitList(it)
                Snackbar.make(
                        editText,
                        "${it.size} result${if (it.size > 1 || it.size == 0) "s" else ""}",
                        Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) showKeyboard()
            else v.hideKeyboard()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (s.isNotBlank() || s.isNotEmpty()) {
                        job.cancel()
                        job = launch {
                            delay(SEARCH_DEBOUNCE_MS)
                            viewModel.searchVerse(s.toString()).observe(viewLifecycleOwner, Observer { list ->
                                list?.let {
                                    // When we receive the list, use it.
                                    adapter.submitList(it)
                                    Snackbar.make(
                                            editText,
                                            "${it.size} result${if (it.size > 1 || it.size == 0) "s" else ""}",
                                            Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    }


                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        rv.adapter = adapter
    }


}