OVERVIEW
------------

Guild Wars 2 is an online video game. It contains a trading post, in which you can both
buy and sell virtual items, either instantly or by placing a bid and waiting for it to be filled.
By investing in certain items and having their prices later increase,
or placing low buy orders and then high sell orders for the same item (i.e. flipping), 
you can make a lot of virtual gold. However, doing this correctly requires a lot of time
spent digging through price listings to try and find the best opportunity.
Quaggy is designed to simplify this process by keeping an up-to-date database of
relevant item and price information. Users can then apply their own custom filters
to this data in order to instantly find the items they believe to be the best opportunities.
The goal for this project is a simple, easy to use interface that allows filtering the large number
of items, with up-to-date trading information, with minimal delay.


BUILD INSTRUCTIONS
------------

After cloning the repo, follow the following instructions.

QuaggyEngine (backend):

 * Download and install the Java 8 JDK, gradle and eclipse with the gradle plugin.
 * Create a new eclipse project in the directory QuaggyEngine.
 * Navigate to the QuaggyEngine eclipse folder and run 'gradle clean build' followed by 'gradle eclipse'.

Backing MySQL store:
 * Download and install a MySQL server, running on localhost port 3306.
 * Create a database named quaggy with user root, password root. You can also use a different user
   if you customize the settings in DB.java in QuaggyEngine.
 * Run DBInit.java in QuaggyEngine to initialize your database.
 * Run DBHistorySync.java in QuaggyEngine to pull all history into the database. 
   (This will take a tremendous amount of time).
 * 

QuaggyEdge (edge servers):
 
 * Download and install python 2.7 and virtualenv.
 * Navigate to QuaggyEdge folder and run 'virtualenv venv' to create a virtual environment.
 * Activate the virtual environment by running '. venv/bin/activate' (Linux) 
   or 'venv\Scripts\activate' (Windows)
 * Run 'pip install -r requirements.txt' to update your virtual environment.

To run the project:

 * Follow all three sets of build instructions above.
 * Begin by running the edge server. In the python virtualenv, navigate to QuaggyEdge and run 
   'python app.py'
 * Run the backend. You can run the Java file executables/QuaggyEngine.java in eclipse.
 * Wait some time for QuaggyEngine to populate the frontend cache with initial values 
   (this is currently a bit slow).
 * In QuaggyEdge, you can run the tests in tests/custom_tests and verify that the output looks good.

DESIGN
------------

The backend of this project is supported by a Java program, with a backing MySQL database for 
storing static item information and history. This program repeatedly queries a Guild Wars 2 API
to learn up-to-date trading post information. After receiving new information about an item, it computes
a large amount of useful data, called features, for this item. This includes information like standard deviation of
price, z-score of current price, current flip profit, etc. Ultimately, this information is what will be available
to the user for filtering.

In order to accurately compute many of these features, the item ID alone is not enough. Most also depend on other
information, such as the number of days of history to consider, and the buy mode and sell mode (Bid or Instant).
Because computing these features is not instantaneous, we pre-compute features for every combination of
buy mode, sell mode, and number of days of history (from an allowed list) for each item ID. As this information is
computed, it is forwarded to the edge servers to update their caches.

The edge servers are implemented in Python and maintain this cache of recent feature information. 
They are also responsible for storing user information and saved filters, and handling requests from users 
to apply their filters. Upon receiving these requests, they must quickly and accurately apply the
specified filter to the cached data and return only the matching items to the user. Because
the feature vector information does not have to be computed at request time, this can be done very quickly.


FILTER OPTIONS
------------

For a complete list of the features that are available for filtering, see FeatureVector.java (in the backend),
or gw2Validate.py (in the frontend).

Filters must be sent to the edge servers using a specific JSON format. An example is included below:

{
  "HistoryDays": 5,
  "BuyMode": "Instant",
  "SellMode": "Bid",
  "SortBy": "MeanProfit",
  "SortOrder": "Desc",
  "Types": 
  [
    "CraftingMaterial",
    "Bag",
    "Mini",
    "Gizmo"
  ],

  "Bounds": 
  {
    "MeanProfit": 
    {
      "Min": 0.15
    },
    
    "OurBuyPrice":
    {
      "Min": 70
    },

    "NumBuyOrders": 
    {
      "Min": 3000
    },

    "NumSellOrders": 
    {
      "Min": 3000
    }
  }
}


TODO AND FUTURE PLANS
------------

This is a work in progress. Some high-priority goals are listed below:

 * The feature vector computations are too slow. Presently each feature is computed independently 
   for each history day value, but this may not be necessary. Many features (for instance, mean sell price)
   do not need to be computed independently (i.e. starting from scratch to compute mean sell price over
   10 days after just computing it over 9 days is a waste of resources)
 
 * Feature vector computations can be computed in parallel, possibly over multiple 
   backend servers to increase speed.

 * Modify QuaggyEngine so that only the most recent few months of data are stored in memory,
   and history that is so old it is irrelevant is left on disk only.

 * Better indexing on QuaggyEdge for more efficient filtering.

 * Currently, updated feature vector information is sent to all edge servers for every full item 
   that is completed. Investigate possibly sending this updated data more or less frequently and 
   examine the effects on performance.

 * Change the communication between backend and edge servers to be something other than a standard 
   API endpoint. This must be secure so that the edge will only accept data from legitimate 
   backend servers. Also, update backend's RESTClient.java to no longer use deprecated Http classes.

 * Replace current tests with unit tests for endpoints.

 * Create a simple web page with functionality to login/logout and create/store/apply filters.

 * Set up the server with gunicorn/nginx and migrate to AWS or other cloud platform.

Some more longer-term goals are listed below:

 * Use entire history and machine learning to attempt to predict best filters for different 
   types of success.

 * Incorporate current GW2 news from official site, forums, and subreddit as features to attempt to 
   predict market trends

 * Support for APIs other than SpidyAPI, which has become largely unsupported.

 * Support for users to manage the items they have purchased, and possibly recommend when and how to sell.