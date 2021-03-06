/*
 * Copyright (c) 2013, Expedia Affiliate Network
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that redistributions of source code
 * retain the above copyright notice, these conditions, and the following
 * disclaimer. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Expedia Affiliate Network or Expedia Inc.
 */

package com.ean.mobile.task;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ean.mobile.Destination;
import com.ean.mobile.app.SampleConstants;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.request.DestinationRequest;
import com.ean.mobile.request.RequestProcessor;

/**
 * Gets suggestions to display based on the current contents of the search bar.
 */
public final class SuggestionFactory {
    private static SuggestDestinationTask suggestDestinationTask;

    /**
     * Private no-op constructor to prevent instantiation.
     */
    private SuggestionFactory() {
        throw new UnsupportedOperationException("Do not use an instance of this class, instead use the factory.");
    }

    /**
     * If there is a suggestion task in progress, kill it. Used to abort current suggestions if they are in progress.
     */
    public static void killSuggestDestinationTask() {
        if (suggestDestinationTask != null && suggestDestinationTask.getStatus() == AsyncTask.Status.RUNNING) {
            suggestDestinationTask.cancel(true);
        }
    }

    /**
     * Gets the list of suggestions and automatically populates an ArrayAdapter with suggestions.
     * @param query The query to get suggestions for.
     * @param suggestionAdapter The adapter to place the suggestions into.
     */
    public static void suggest(final String query, final ArrayAdapter<Destination> suggestionAdapter) {
        killSuggestDestinationTask();
        suggestDestinationTask = new SuggestDestinationTask(suggestionAdapter);
        suggestDestinationTask.execute(query);
    }

    private static final class SuggestDestinationTask extends AsyncTask<String, Integer, List<Destination>> {

        private static final int NUMBER_OF_CITIES_TO_DISPLAY = 6;

        private final ArrayAdapter<Destination> suggestionAdapter;

        private SuggestDestinationTask(final ArrayAdapter<Destination> suggestionAdapter) {
            this.suggestionAdapter = suggestionAdapter;
        }

        @Override
        protected List<Destination> doInBackground(final String... strings) {
            try {
                return RequestProcessor.run(new DestinationRequest(strings[0]));
            } catch (EanWsError ewe) {
                Log.d(SampleConstants.LOG_TAG, "The API call returned an error", ewe);
            } catch (UrlRedirectionException ure) {
                Log.d(SampleConstants.LOG_TAG, "The API call has been unexpectedly redirected!", ure);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Destination> destinations) {
            if (destinations != null) {
                suggestionAdapter.clear();
                for (Destination destination : destinations) {
                    if (destination.category == Destination.Category.CITY) {
                        suggestionAdapter.add(destination);
                        if (suggestionAdapter.getCount() >= NUMBER_OF_CITIES_TO_DISPLAY) {
                            break;
                        }
                    }
                }
                suggestionAdapter.notifyDataSetChanged();
            }
        }
    }
}