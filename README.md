# android-facebook-tumblr-snap
Tumblr client that supports viewing a stream of photos taken by people 

## Overview

The objective of this exercise is to understand and implement intent service. IntentService is a base class for Service that handles async request on demand.

Clients send requests through startService(Intent) calls; the service is started as needed, handles each Intent in turn using a worker thread, and stops itself when it runs out of work.

## Usage
This app is intended to be the base project on top of which new features can be added. We will implement photo upload functionality in this exercise using IntentService.

To use it, clone the project and import it using the following steps:

![Imgur](http://i.imgur.com/x5iXb8Y.gif)

Once you have imported base app, go ahead and run it, and you should see the following output : 
  
![Imgur](http://i.imgur.com/aHlnOdXm.png)

## Now, let's understand the steps involved in creating and executing IntentService :

**Create your service by extending IntentService**

```
public class TumblrUploadService extends IntentService
```

**Override onHandleIntent** - This method is invoked on the worker thread with a request to process. Only one Intent is processed at a time.

```
@Override
protected void onHandleIntent(Intent intent) 
{
  // do stuff on worker thread
  // This is where you will opne Http Connection to upload the bitmap
}
```

**Launch the service from your activity/fragment**

```
Intent uploadIntent = new Intent(this, TumblrUploadService.class);
msgIntent.putExtra("key", data);
startService(uploadIntent);
```

The service takes over from here, catching each intent request, processing it, and shutting itself down when it’s all done

**Define the Broadcast Receiver** - this needs to be done if activity/fragment needs to know and visually update user that request has been processed

```
public class ResponseReceiver extends BroadcastReceiver
```

**Override onReceive of the receiver**

```
@Override
public void onReceive(Context context, Intent intent) 
{
   //get data from intent and update UI
}
```

**Broadcast the Result** - result broadcasting is done from onHandleIntent of the service

```
Intent broadcastIntent = new Intent();
broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
broadcastIntent.putExtra("key", "data");
sendBroadcast(broadcastIntent);
```

**Register the broadcast receiver** - Register the receiver in the onCreate() method with the appropriate intent filter to catch the specific result intent being sent from the IntentService

```
IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
filter.addCategory(Intent.CATEGORY_DEFAULT);
ResponseReceiver receiver = new ResponseReceiver();
registerReceiver(receiver, filter);
```
