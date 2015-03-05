<html>
<head>
<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {

		$("#joineeDetailsForm").submit(function() {
			var url = "recommender.do";
			$.ajax({
				type : "POST",
				url : url,
				data : $("#joineeDetailsForm").serialize(), 
				success : function(data) {
					$("#responseContent").html(data);
				}
			});

			return false;
		});
	})
</script>
</head>
<body>
	<h3>Buddy recommender</h3>
	<br />
	<form action="recommender.do" method="post" id="joineeDetailsForm">
		<table>
			<tr>
				<td><label>Joinee Name:</label></td>
				<td><input type="text" name="name" /></td>
			</tr>
			<tr>
				<td><label>Joinee Place:</label></td>
				<td><input type="text" name="place" /></td>
			</tr>
			<tr>
				<td><label>Joinee college:</label></td>
				<td><input type="text" name="college" /></td>
			</tr>
			<tr>
				<td><label>Joinee's previous organizations(comma
						separated):</label></td>
				<td><input type="text" name="previousOrganizations" /></td>
			</tr>
			<tr>
				<td><label>Joinee's skills(comma separated):</label></td>
				<td><input type="text" name="skills" /></td>
			</tr>
			<tr>
				<td><label>Joinee's experience:</label></td>
				<td><input type="text" name="experience" /></td>
			</tr>
			<tr>
				<td><label>Joinee's team:</label></td>
				<td><input type="text" name="team" /></td>
			</tr>
			<tr>
				<td><input type="submit" /></td>
			</tr>
		</table>
		<label></label>
	</form>
	<br />
	<div id="responseContent"></div>
</body>
</html>
