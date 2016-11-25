package cl.telematica.android.certamen3v2.Views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cl.telematica.android.certamen3v2.Presenters.MemorizePresenterImpl;
import cl.telematica.android.certamen3v2.R;
import cl.telematica.android.certamen3v2.adapters.GridAdapter;
import cl.telematica.android.certamen3v2.interfaces.CardSelectedListener;
import cl.telematica.android.certamen3v2.interfaces.DialogListener;
import cl.telematica.android.certamen3v2.interfaces.MemorizePresenter;
import cl.telematica.android.certamen3v2.models.CardModel;
import cl.telematica.android.certamen3v2.utils.MessageFactory;
import cl.telematica.android.certamen3v2.utils.Utils;

public class MemorizeActivity extends AppCompatActivity implements CardSelectedListener {

    public GridView gridView;
    public TextView scoreText;
    public List<CardModel> cards;
    public int counter = 0;
    public int lastPosition = 0;
    public int pairsCounter = 0;
    public int score = 0;
    public CardModel lastCard;
    public GridAdapter adapter;

    private static final String KEY_COUNTER = "Memorize::Counter";
    private static final String KEY_LAST_POSITION = "Memorize::LastPosition";
    private static final String KEY_PAIRS_COUNTER = "Memorize::PairsCounter";
    private static final String KEY_SCORE = "Memorize::Score";


    MemorizePresenter memorizePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);

        if ((savedInstanceState != null)){
            if(savedInstanceState.containsKey(KEY_COUNTER)) {
                counter = savedInstanceState.getInt(KEY_COUNTER);
            }
            if(savedInstanceState.containsKey(KEY_LAST_POSITION)) {
                lastPosition = savedInstanceState.getInt(KEY_LAST_POSITION);
            }
            if(savedInstanceState.containsKey(KEY_PAIRS_COUNTER)) {
                pairsCounter = savedInstanceState.getInt(KEY_PAIRS_COUNTER);
            }
            if(savedInstanceState.containsKey(KEY_SCORE)) {
                score = savedInstanceState.getInt(KEY_SCORE);
            }
        }

        /*Button scoreBtn = (Button)findViewById(R.id.scoreButton);
        scoreBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent i = new Intent(MemorizeActivity.this, RankingActivity.class);
                startActivity(i);
            }
        });*/

        createVariables();

        setGridData();
        memorizePresenter = new MemorizePresenterImpl(MemorizeActivity.this, getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memorize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.scoreButton:
                /**
                 * Call to the ranking
                 */
                /*Intent intent = new Intent(MemorizeActivity.this, RankingActivity.class);
                startActivity(intent);*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createVariables(){
        gridView = (GridView) findViewById(R.id.cardsGrid);
        scoreText = (TextView) findViewById(R.id.scoreText);
    }

    public void setGridData(){
        counter = 0;
        lastPosition = 0;
        pairsCounter = 0;
        score = 0;

        cards = Utils.createCards();

        adapter = new GridAdapter(getApplicationContext(),
                R.id.scoreText,
                cards,
                this);
        gridView.setAdapter(adapter);

        String scoreString = String.format(getString(R.string.score_title),
                String.valueOf(score));

        scoreText.setText(scoreString);
    }

    @Override
    public void onCardSelected(CardModel card, int position) {
        if(++counter == 2 && lastCard != null){
            if(lastCard.getCard() == card.getCard()){
                if(++pairsCounter == 8){
                    Dialog dialog = MessageFactory.createDialog(MemorizeActivity.this, new DialogListener() {

                        @Override
                        public void onCancelPressed(DialogInterface dialog) {
                            dialog.dismiss();
                            setGridData();
                        }

                        @Override
                        public void onAcceptPressed(DialogInterface dialog, String name) {
                            if(!Utils.isEmptyOrNull(name)){
                                dialog.dismiss();

                                memorizePresenter.dbInsert(name, score);
                                /**
                                 * Call to the ranking
                                 */
                               Intent intent = new Intent(MemorizeActivity.this, RankingActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("score", score);
                                setGridData();
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.dialog_down_text),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.winned_text),
                            Toast.LENGTH_SHORT).show();
                }
                score = score + 2;
                counter = 0;
                lastCard = null;

                String scoreString = String.format(getString(R.string.score_title),
                        String.valueOf(score));

                scoreText.setText(scoreString);
            } else {
                scoreText.setText(getString(R.string.bad_text));
                executePostDelayed(card, position);
            }
        } else {
            lastCard = card;
            lastPosition = position;
        }
    }

    public void executePostDelayed(final CardModel card, final int position){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                lastCard.setSelected(false);
                card.setSelected(false);

                cards.set(lastPosition, lastCard);
                cards.set(position, card);

                adapter.notifyDataSetChanged();

                counter = 0;
                lastCard = null;

                String scoreString = String.format(getString(R.string.score_title),
                        String.valueOf(--score));

                scoreText.setText(scoreString);
            }
        }, 500);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COUNTER, counter);
        outState.putInt(KEY_LAST_POSITION, lastPosition);
        outState.putInt(KEY_PAIRS_COUNTER, pairsCounter);
        outState.putInt(KEY_SCORE, score);
    }

    public void scoreClick ( MenuItem item ) {
        Intent i = new Intent(MemorizeActivity.this, RankingActivity.class);
        startActivity(i);
    }
}
