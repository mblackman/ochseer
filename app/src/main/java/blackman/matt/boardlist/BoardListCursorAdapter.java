/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package blackman.matt.boardlist;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Space;
import android.widget.Toast;
import android.widget.ToggleButton;

import blackman.matt.infinitebrowser.R;

/**
 * Reads in all the rows from the SQL query and addresses them to listCardViews and sets up
 * the adapter.
 *
 * Created by Matt on 10/24/2014.
 */
public class BoardListCursorAdapter extends CursorAdapter {
    private String mSelectedValue;
    private String mSortOrder;
    private Context mContext;
    private Cursor mCursor;
    private final EditText mSearchBar;

    static class ViewHolder {
        BoardListCardView mView;
    }

    /**
     * Basic constructor for the class. Runs the parent constructor and assigns values.
     *
     * @param context Context of caller.
     * @param c Cursor to be adapted.
     * @param selectedValue The currently selected query column.
     */
    public BoardListCursorAdapter(Context context, Cursor c, String selectedValue, String sort,
                                  EditText searchBar) {
        super(context, c, 0);
        this.mContext = context;
        this.mCursor = c;
        this.mSelectedValue = selectedValue;
        this.mSortOrder = sort;
        this.mSearchBar = searchBar;
    }

    /**
     * Called when a new view is going to be created, and it creates the new CardListView.
     *
     * @param context context of the caller.
     * @param cursor Cursor for the query.
     * @param parent Parent view group to the caller.
     * @return The newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new BoardListCardView(context);
    }

    /**
     * Binds data to the view once the view has been inflated and data read.
     *
     * @param view View to be binded.
     * @param context Context of the caller.
     * @param cursor Cursor for the query.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String boardName;
        String nationality;
        String displayColumn;
        final String boardLink;
        int favoritedInt;
        boolean isFavorited;

        boardName = cursor.getString(cursor.getColumnIndexOrThrow("boardname"));
        boardLink = cursor.getString(cursor.getColumnIndexOrThrow("boardlink"));
        nationality = cursor.getString(cursor.getColumnIndexOrThrow("nation"));
        displayColumn = cursor.getString(cursor.getColumnIndexOrThrow(mSelectedValue));
        favoritedInt = cursor.getInt(cursor.getColumnIndexOrThrow("favorited"));

        isFavorited = favoritedInt > 0;

        ((BoardListCardView) view).setCardInfo(boardLink,
                boardName,
                nationality,
                displayColumn,
                isFavorited
        );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(mCursor.moveToPosition(position)) {
            final ViewHolder holder;
            final ToggleButton viewToggle;
            final String boardLink;
            final CursorAdapter myAdapter = this;

            String boardName;
            String nationality;
            String displayColumn;
            int favoritedInt;
            boolean isFavorited;

            if(convertView == null || convertView instanceof Space) {
                convertView = new BoardListCardView(mContext);
                holder = new ViewHolder();
                holder.mView = (BoardListCardView) convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            nationality = mCursor.getString(1);
            boardLink = mCursor.getString(2);
            boardName = mCursor.getString(3);
            favoritedInt = mCursor.getInt(4);
            displayColumn = mCursor.getString(5);

            isFavorited = favoritedInt > 0;

            viewToggle = (ToggleButton) holder.mView.findViewById(R.id.tb_board_fav);

            viewToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((ToggleButton) v).isChecked();
                    BoardListDatabase list_db = new BoardListDatabase(mContext);
                    CharSequence text;
                    int duration = Toast.LENGTH_SHORT;

                    if(isChecked) {
                        text = "Added Board " + boardLink;
                    } else {
                        text = "Removed Board " + boardLink;
                    }

                    Toast toast = Toast.makeText(mContext, text, duration);
                    toast.show();

                    list_db.favoriteBoard(boardLink, isChecked);

                    if(mSearchBar.getVisibility() == View.VISIBLE) {
                        String search = mSearchBar.getText().toString();
                        mCursor = list_db.getSortedSearch(search,mSelectedValue, mSortOrder);
                    } else {
                        mCursor = list_db.getBoardsInSortedOrder(mSelectedValue, mSortOrder);
                    }
                    myAdapter.swapCursor(mCursor);
                    myAdapter.notifyDataSetChanged();

                    holder.mView.invalidate();
                }
            });

            ((BoardListCardView) convertView).setCardInfo(boardLink,
                    boardName,
                    nationality,
                    displayColumn,
                    isFavorited
            );
        } else {
            convertView = new Space(mContext);
            convertView.setVisibility(View.GONE);
            Log.i("Cursor no show", "couldn't move cursor to position " + position);
        }

        return convertView;
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        return oldCursor;
    }

    public void setDatabaseValues(String newOrder, String newSort) {
        mSelectedValue = newOrder;
        mSortOrder = newSort;
    }
}