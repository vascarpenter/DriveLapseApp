package com.hatenablog.gikoha.drivelapseapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import com.hatenablog.gikoha.drivelapseapp.ui.theme.DriveLapseAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val routes = resources.getStringArray(R.array.routes)
        setContent {
            MainScreen(routes)
        }
    }
}

@Composable
fun MainScreen(routes: Array<String>)
{
    val focusManager = LocalFocusManager.current

    val viewModel: DriveLapseViewModel = hiltViewModel()

    val buttontitle by viewModel.buttontitle.observeAsState()

    val viewState: DriveLapseViewState by viewModel.state.collectAsState(initial = DriveLapseViewState.EMPTY)
    val items: List<DriveLapse> = viewState.items ?: emptyList()
    if (items.isEmpty())
    {
        viewModel.loadData {
            // scroll不要
            //viewModel.changeTitle("SUBMIT")  // replace to initial value
        }
    }

    DriveLapseMainScreen(items, buttontitle, routes) { direction, deptime, p1time, p2time, p3time, arrtime, route ->
        focusManager.clearFocus()

        //Log.i("post", String.format("%s:%s %s %s %s %s %s", direction, deptime, p1time, p2time, p3time, arrtime, route))

        viewModel.postData(direction, deptime, p1time, p2time, p3time, arrtime, route)
        {
            viewModel.changeTitle("POST OK")
            viewModel.clearData()
            // 削除すると recompose 行われ、loadData が行われ、再度 recomposeされることで recompose保証
            // updateだけでは recomposeすら行われない「ことがある」
        }

    }

}

class OneLineParameterProvider : PreviewParameterProvider<DriveLapse>
{
    override val values = sequenceOf(
        DriveLapse(
            "自宅→勤務", "04/13", "水曜日",
            "0646", "0713", "27", "0657", "0702", "0709",
            "自宅→P1:東京→P2:有楽町→P3:新橋→浜松町", "晴れ"
        ),
    )
}

// Create one line row in Compose LazyColumn
@Preview(name = "Oneline")
@Composable
fun DriveLapseOneline(
    @PreviewParameter(OneLineParameterProvider::class) data: DriveLapse
)
{
    Row {
        Text(
            text = data.depdate,
            modifier = Modifier.width(60.dp).padding(all = 4.dp)
                .align(alignment = Alignment.CenterVertically)
        )

        Column {

            Text(
                text = data.route ?: "Not Set",
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().padding(all = 4.dp)
            )
            Text(
                text = String.format("%s %s", data.deptime, data.arrivetime),
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().padding(all = 4.dp)
            )
        }
    }

}

@Preview(name = "timeButton")
@Composable
fun TimeButton(
    title: String = "出発時刻",
    text: String = "",
    onClick: (String) -> Unit = { _ -> },
    onValueChange: (String) -> Unit = { _ -> }
)
{
    Row {
        Text(
            text = title,
            modifier = Modifier.width(70.dp).align(alignment = Alignment.CenterVertically)
                .padding(all = 4.dp),
            fontSize = 10.sp
        )
        TextField(
            value = text,
            onValueChange = onValueChange,  // 値を代入するものはすべて closureとして Compose内から追い出す
            modifier = Modifier.padding(all = 4.dp)
                .width(160.dp),
            maxLines = 1,
            singleLine = true,
        )
        Button(
            onClick = {  // clickで TimeをSETしてくれるような closureを呼び出す
                onClick(text)
            },
            modifier = Modifier.align(alignment = Alignment.CenterVertically)
                .padding(all = 4.dp)
        ) {
            Text(
                text = "SET",
                fontSize = 10.sp,
            )
        }
    }
}

class MainScreenParameterProvider : PreviewParameterProvider<List<DriveLapse>>
{
    private val d = DriveLapse(
        "自宅→勤務", "04/13", "水曜日",
        "0646", "0713", "27", "0657", "0702", "0709",
        "自宅→P1:新橋→P2:宇佐美→P3:拾ヶ堰橋北→成相", "晴れ"
    )
    private val e = d.copy(depdate = "04/14")
    override val values = sequenceOf(
        listOf(d, e),
    )
}

@Preview(name = "MainScreen")
@Composable
fun DriveLapseMainScreen(
    @PreviewParameter(MainScreenParameterProvider::class) items: List<DriveLapse>,
    buttontitle: String? = "SUBMIT",
    routes: Array<String> = arrayOf("route1", "route2"),
    onClick: (
        direction: String,
        depTime: String,
        p1Time: String,
        p2Time: String,
        p3Time: String,
        arrTime: String,
        route: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
)
{
    var deptime by remember { mutableStateOf("") }
    var p1time by remember { mutableStateOf("") }
    var p2time by remember { mutableStateOf("") }
    var p3time by remember { mutableStateOf("") }
    var arrtime by remember { mutableStateOf("") }
    val selectionStates = remember { mutableStateOf(0) }

    DriveLapseAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {

            Column {
                TopAppBar(
                    title = { Text("DriveLapse") },
                )

                // RadioButton groups
                Column(Modifier.selectableGroup()) {
                    routes.forEachIndexed { ind, text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .selectable(
                                    selected = (selectionStates.value == ind),
                                    onClick = { selectionStates.value = ind },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (ind == selectionStates.value),
                                onClick = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }

                TimeButton("出発時刻", deptime, onClick = { deptime = getDateTimeString() })
                {
                    deptime = it
                }
                TimeButton("経由点1", p1time, onClick = { p1time = getTimeString() })
                {
                    p1time = it
                }
                TimeButton("経由点2", p2time, onClick = { p2time = getTimeString() })
                {
                    p2time = it
                }
                TimeButton("経由点3", p3time, onClick = { p3time = getTimeString() })
                {
                    p3time = it
                }
                TimeButton("到着時刻", arrtime, onClick = { arrtime = getTimeString() })
                {
                    arrtime = it
                }

                Button(
                    onClick = {
                        val sel = selectionStates.value
                        val direction = if (sel < routes.count())
                        {
                            "0"
                        } else
                        {
                            "1"
                        } // 0:行き 1:帰り
                        val dateStr = deptime.substring(0, 8) + " " // e.g. 20220827

                        onClick(
                            direction,
                            deptime,
                            dateStr + p1time,
                            dateStr + p2time,
                            dateStr + p3time,
                            dateStr + arrtime,
                            routes[sel]
                        )
                    },
                    modifier = Modifier.padding(all = 4.dp)
                ) {
                    Text(
                        text = buttontitle ?: "",
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }

                DriveLapseLists(items)
            }


        }
    }
}

@Composable
fun DriveLapseLists(items: List<DriveLapse>)
{
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items) { _, item ->
            DriveLapseOneline(item)
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}

fun getDateTimeString(): String
{
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    return String.format("%04d%02d%02d %02d%02d", year, month, day, hour, minute)
}

fun getTimeString(): String
{
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    return String.format("%02d%02d", hour, minute)
}
