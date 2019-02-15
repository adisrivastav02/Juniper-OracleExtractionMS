<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<script>
$(document).ready(function() {
	document.getElementById('counter').value="${counter_val}";
	document.getElementById('but').style.display = "block";
});
document.getElementById('bord').style.display = "block";
</script>
<input type="hidden" name="connection_id" id="connection_id"
	class="form-control" value="${conn_val.connection_id}">
<input type="hidden" name="counter" id="counter" class="form-control"
	value="1">
<div class="form-group">
	<label>Schema Name *</label> <select name="schema_name"
		id="schema_name" class="form-control" readonly="readonly">
		<option value="${schem}">${schem}</option>
	</select>
</div>
<div id="ind_load"
	style="border: 1px groove #DCDCDC; border-radius: 10px; padding: 10px;"
	style.display="none";>
	<div id="tbl_fld">
		<c:forEach items="${arrddb}" var="arrddb" varStatus="theCount">
			<div id="delete${theCount.count}" style="float: right;">
				<button id="del${theCount.count}" type="button"
					class="btn btn-rounded btn-gradient-danger mr-2"
					onclick="delblock(this.id.slice(-1))">X</button>
			</div>
			<div class="form-group row">
				<div class="col-sm-6">
					<label>Select Table *</label> <select class="form-control"
						id="table_name${theCount.count}"
						name="table_name${theCount.count}"
						onchange="getcols(this.id,this.value)">
						<c:forEach items="${tables}" var="tables">
							<c:choose>
								<c:when test="${arrddb.table_name_short==tables}">
									<option value="${schem}.${tables}" selected>${tables}</option>
								</c:when>
								<c:otherwise>
									<option value="${schem}.${tables}">${tables}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
				<div class="col-sm-6">
					<label>Load Type *</label> <select class="form-control"
						id="fetch_type${theCount.count}"
						name="fetch_type${theCount.count}"
						onchange="incr(this.id,this.value)">
						<c:choose>
							<c:when test="${arrddb.fetch_type=='full'}">
								<option value="full" selected>Full Load</option>
								<option value="incr">Incremental Load</option>
							</c:when>
							<c:otherwise>
								<option value="full">Full Load</option>
								<option value="incr" selected>Incremental Load</option>
							</c:otherwise>
						</c:choose>
					</select>
				</div>
			</div>
			<div id="fldd${theCount.count}">
				<c:choose>
					<c:when test="${arrddb.fetch_type=='incr'}">
						<div class="form-group" id="incc${theCount.count}"
							style="display: block;">
							<label>Select Incremental Column *</label> <select
								class="form-control" id="incr_col${theCount.count}"
								name="incr_col${theCount.count}">
								<c:forTokens items="${arrddb.cols}" delims="," var="mySplitx">
									<c:choose>
										<c:when test="${arrddb.incremental_column==mySplitx}">
											<option value="${mySplitx}" selected>${mySplitx}</option>
										</c:when>
										<c:otherwise>
											<option value="${mySplitx}">${mySplitx}</option>
										</c:otherwise>
									</c:choose>
								</c:forTokens>
							</select>
						</div>
					</c:when>
					<c:otherwise>
						<div class="form-group" id="incc${theCount.count}"
							style="display: none;">
							<label>Select Incremental Column *</label> <select
								class="form-control" id="incr_col${theCount.count}"
								name="incr_col${theCount.count}">
								<c:forTokens items="${arrddb.cols}" delims="," var="mySplitx">
									<c:choose>
										<c:when test="${arrddb.incremental_column==mySplitx}">
											<option value="${mySplitx}" selected>${mySplitx}</option>
										</c:when>
										<c:otherwise>
											<option value="${mySplitx}">${mySplitx}</option>
										</c:otherwise>
									</c:choose>
								</c:forTokens>
							</select>
						</div>
					</c:otherwise>
				</c:choose>
				<div class="form-group">
					<label>Select Columns *</label> <select class="form-control"
						id="col_name${theCount.count}" name="col_name${theCount.count}"
						multiple="multiple">
						<option value="*">Select All</option>
						<c:forTokens items="${arrddb.cols}" delims="," var="mySplit">
							<c:set var="val" scope="session" value="0" />
							<c:forTokens items="${arrddb.column_name}" delims=","
								var="mySplit1">
								<c:choose>
									<c:when test="${mySplit1==mySplit}">
										<option value="${mySplit}" selected>${mySplit}</option>
										<c:set var="val" scope="session" value="1" />
									</c:when>
									<c:otherwise>
									</c:otherwise>
								</c:choose>
							</c:forTokens>
							<c:if test="${val==0}">
								<option value="${mySplit}">${mySplit}</option>
							</c:if>
						</c:forTokens>
					</select>
				</div>
				<input type="hidden" name="columns_name${theCount.count}"
					id="columns_name${theCount.count}" class="form-control">
				<script>
					document.getElementById('counter').value = '${theCount.count}';
					var cnt = document.getElementById('counter').value;
					var select = document.getElementById('col_name' + cnt);
					multi(select, {});
				</script>
			</div>
			<div class="form-group">
				<label>Where Condition *</label>
				<textarea class="form-control" id="where_clause${theCount.count}"
					name="where_clause${theCount.count}" style="width: 100%;"
					placeholder="column1='filter1' and (column2>'filter2' or column3<'filter3')">${arrddb.where_clause}</textarea>
			</div>
		</c:forEach>
	</div>
	<div class="form-group"
		style="float: right; margin: 5px; margin-top: 10px;">
		<button id="upd" type="button"
			class="btn btn-rounded btn-gradient-info mr-2"
			onclick="return dup_div();">+</button>
	</div>
</div>
<button onclick="jsonconstruct();"
	class="btn btn-rounded btn-gradient-info mr-2">Update</button>