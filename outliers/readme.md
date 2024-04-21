### Backend – Identify outliers of timeseries data (Stock price)
## How to run
Prerequisites:
* Python 3.12 is installed.
* Clone the project.
* Update/modify (if needed) the source data from <code>data/stock_price_data_files</code>
* Run <code>python main.py</code>
* Check the outputs from <code>data/output</code>
# Code Structure
* <code>lseg</code> package contains the public library - mechanisms to load, parse, return data.
* <code>main.py</code> contains all the client code that is consuming the <code>lseg</code> functions.
* The two required functions (<code>get_30_consecutive_datapoints</code> and <code>get_outliers</code>)
are part of client's code (<code>main.py</code>)
* in case of parsing failure, the code should fail with asserted messages.