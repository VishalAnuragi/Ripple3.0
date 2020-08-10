package com.example.ripple20.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ripple20.R
import com.example.ripple20.fragments.SongPlayingFragment
import com.example.ripple20.utils.Songs

class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>(), Filterable {
    var songDetails: ArrayList<Songs>? = null
    var songDetailsFilter: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
        this.songDetailsFilter = _songDetails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {

        val songObject = songDetailsFilter?.get(position)
        holder?.trackTitle?.text = songObject?.songTitle
        holder?.trackArtist?.text = songObject?.artist
        holder?.contentHolder?.setOnClickListener({
            SongPlayingFragment.Statified.mediaPlayer?.pause()

            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("songTitle", songObject?.songTitle)
            args.putString("path", songObject?.songData)
            args.putInt("songId", songObject?.songId?.toInt() as Int)

            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetailsFilter)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragment")
                .commit()
        })

    }

    override fun getItemCount(): Int {
        if (songDetailsFilter == null) {
            return 0
        } else {
            return (songDetailsFilter as ArrayList<Songs>).size
        }
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = itemView?.findViewById<TextView>(R.id.trackTitle)
            trackArtist = itemView?.findViewById<TextView>(R.id.trackArtist)
            contentHolder = itemView?.findViewById<RelativeLayout>(R.id.contentRow)
        }

    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(constraint: CharSequence): FilterResults? {

                val character = constraint.toString()
                if (character.isEmpty()){
                    songDetailsFilter = songDetails
                } else{
                    val filterList = ArrayList<Songs>()
                    for (row in songDetails!!) {
                        if (row.songTitle.toLowerCase().contains(character.toLowerCase())){
                            filterList.add(row)
                        }
                    }

                    songDetailsFilter = filterList
                }

                val filterResults = FilterResults()
                filterResults.values = songDetailsFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {

                songDetailsFilter = results.values as ArrayList<Songs>
                notifyDataSetChanged()
            }
        }
    }
}