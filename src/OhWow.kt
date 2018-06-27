import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Arrays.asList

class OhWow{
	var okno : BasicWindow
	var okno2 : BasicWindow
	var okno3 : BasicWindow
	var finalgui : MultiWindowTextGUI
	lateinit var echoSocket : Socket
	lateinit var toServer : PrintWriter
	lateinit var fromServer : BufferedReader
	init {
		var gui = DefaultTerminalFactory().createTerminal()
		var screen = TerminalScreen(gui)
		screen.startScreen()

		var panel = Panel()
		panel.setLayoutManager(GridLayout(2))

		panel.addComponent(Label("IP Adresa:"))
		var ipbox = TextBox()
		panel.addComponent(ipbox)

		panel.addComponent(Label("Port:"))
		var portbox = TextBox()
		panel.addComponent(portbox)

		panel.addComponent(EmptySpace(TerminalSize(0,0)))
		panel.addComponent(Button("OK", { buttonorsomething(ipbox.text, portbox.text.toInt()) }))

		okno = BasicWindow()
		okno.component = panel
		okno.setHints(asList(Window.Hint.CENTERED))

		//Druhé okno
		var panel2 = Panel()
		panel2.setLayoutManager(GridLayout(2))

		panel2.addComponent(Label("Nickname:"))
		var nickbox = TextBox()
		panel2.addComponent(nickbox)

		panel2.addComponent(EmptySpace(TerminalSize(0,0)))
		panel2.addComponent(Button("OK", { chat(nickbox.text) }))

		okno2 = BasicWindow()
		okno2.component = panel2
		okno2.setHints(asList(Window.Hint.CENTERED))

		//Tretie okno
		var panel3 = Panel()
		panel3.setLayoutManager(GridLayout(1))

		var chatbox = TextBox(TerminalSize(80, 10))
		panel3.addComponent(chatbox)

		panel3.addComponent(EmptySpace(TerminalSize(0,8)))

		var chatboxsend = TextBox(TerminalSize(80,1))
		panel3.addComponent(chatboxsend)

		panel3.addComponent(Button("Send", { sendText(chatboxsend.text) }))

		okno3 = BasicWindow()
		okno3.component = panel3
		okno3.setHints(asList(Window.Hint.FULL_SCREEN))


		finalgui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
		finalgui.addWindowAndWait(okno)
	}

	fun buttonorsomething(ip : String, port : Int){
		echoSocket = Socket(ip, port)
		toServer = PrintWriter(echoSocket.getOutputStream(), true)
		fromServer = BufferedReader(InputStreamReader(echoSocket.getInputStream()))

		okno.close()

		if(fromServer.readLine().equals("well")){
			finalgui.addWindowAndWait(okno2)
		}
	}

	fun chat(nick : String){
		okno2.close()
		toServer.println(nick)

		finalgui.addWindowAndWait(okno3)

	}

	fun sendText(text : String){
		toServer.println(text)
	}
}

fun main(args: Array<String>) {
	OhWow()
}