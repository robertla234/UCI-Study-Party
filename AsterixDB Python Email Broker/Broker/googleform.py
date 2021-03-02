def context():

 html="""\
<html>
<head>
<style>
table {
  width:100%;
}
table, th, td {
  border: 1px solid black;
  border-collapse: collapse;
}
th, td {
  padding: 15px;
  text-align: left;
}
#t01 tr:nth-child(even) {
  background-color: #eee;
}
#t01 tr:nth-child(odd) {
 background-color: #fff;
}
#t01 th {
  background-color: black;
  color: white;
}
</style>
</head>
<body>

<h2>Different services you can get</h2>


<br>

<table id="t01">
  <tr>
    <th>services</th>
    <th>Email Subject</th> 
    <th>Example</th>
  </tr>
     <tr>
     <td>Learn about different channels</td>
     <td>request for all channels</td>
     <td>Please send me all the channels which exists for registeration
 </td>
   </tr>
  <tr>
    <td>learning about parameters needed</td>
    <td>getting info about a channel</td>
    <td>I would like to know the information needed for subscription to channel ThreateningTweetsAt .</td>
  </tr>
  <tr>
    <td>subscribing</td>
    <td>register in channel</td>
    <td>Name of the channel :StationsNearMe<br />
 The parameters needed for the subscription:"LA"</td>
  </tr>
</table>
 <br>
 <p1>Please complete the following form: <br />
  https://forms.gle/QBG4TwN6a71kPLuk7
 </p1>
</body>
</html>"""
 return html

