import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Arrays.asList
import kotlin.concurrent.thread

class OhWow{
	private var okno : BasicWindow
	private var okno2 : BasicWindow
	private var okno3 : BasicWindow
	private var chatbox : Table<String>
	private lateinit var finalgui : MultiWindowTextGUI
	private lateinit var nickname : String
	private lateinit var echoSocket : Socket
	private lateinit var toServer : PrintWriter
	private lateinit var fromServer : BufferedReader
	init {
		val gui = DefaultTerminalFactory().createTerminal()
		val screen = TerminalScreen(gui)
		screen.startScreen()

		val panel = Panel()
		panel.layoutManager = GridLayout(2)

		panel.addComponent(Label("IP Adresa:"))
		val ipbox = TextBox()
		panel.addComponent(ipbox)

		panel.addComponent(EmptySpace(TerminalSize(0,1)))
		panel.addComponent(EmptySpace(TerminalSize(0,1)))

		panel.addComponent(Label("Port:"))
		val portbox = TextBox()
		panel.addComponent(portbox)

		panel.addComponent(EmptySpace(TerminalSize(0,1)))
		panel.addComponent(EmptySpace(TerminalSize(0,1)))
		panel.addComponent(Button("Exit", { System.exit(0) }))
		panel.addComponent(Button("OK", { buttonorsomething(ipbox.text, portbox.text) }))

		okno = BasicWindow()
		okno.component = panel
		okno.setHints(asList(Window.Hint.CENTERED))

		//Druhé okno
		val panel2 = Panel()
		panel2.layoutManager = GridLayout(2)

		panel2.addComponent(Label("Nickname:"))
		val nickbox = TextBox()
		panel2.addComponent(nickbox)

		panel2.addComponent(EmptySpace(TerminalSize(0,0)))
		panel2.addComponent(Button("OK", {
			nickname = nickbox.text
			chat(nickbox.text)
		}))

		okno2 = BasicWindow()
		okno2.component = panel2
		okno2.setHints(asList(Window.Hint.CENTERED))

		//Tretie okno
		val panel3 = Panel()
		panel3.layoutManager = GridLayout(1)

		chatbox = Table("Chat")
		chatbox.size = TerminalSize(50,10)
		chatbox.isEnabled = false

		for(i in 0..10){
			chatbox.tableModel.addRow("")
		}

		panel3.addComponent(chatbox)

		panel3.addComponent(EmptySpace(TerminalSize(0,1)))

		val chatboxsend = TextBox(TerminalSize(50,1))
		panel3.addComponent(chatboxsend)

		panel3.addComponent(Button("Send", {
			if (chatboxsend.text != "/disconnect" && chatboxsend.text != "/getnickname") {
				sendText(chatboxsend.text)
			}else{
				MessageDialog.showMessageDialog(finalgui, "Error", "plz dont")
			}
			chatboxsend.text = ""
		}))

		panel3.addComponent(Button("Disconnect", {
			disconnect()
		}))

		okno3 = BasicWindow()
		okno3.component = panel3
		okno3.setHints(asList(Window.Hint.CENTERED))

		finalgui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
		finalgui.addWindowAndWait(okno)
	}

	private fun buttonorsomething(ip : String, port : String) {
		okno.isVisible = false

		try {
			echoSocket = Socket(ip, port.toInt())
			toServer = PrintWriter(echoSocket.getOutputStream(), true)
			fromServer = BufferedReader(InputStreamReader(echoSocket.getInputStream()))
		} catch (e: Exception) {
			MessageDialog.showMessageDialog(finalgui, "Error", "Can't connect to server:\n${e.message}")
			okno.isVisible = true
			return
		}

		if (fromServer.readLine() == "/getnickname") {
			finalgui.addWindowAndWait(okno2)
		} else {
			MessageDialog.showMessageDialog(finalgui, "Error", "Server not ready")
			okno.isVisible = true
			return
		}

		okno.close()
	}

	private fun chat(nick : String){
		okno2.close()
		toServer.println(nick)

		thread {
			var servertext : String
			while (true){
				servertext = fromServer.readLine()
				if (servertext == null){
					break
				} else{
					for (line in splitLine(servertext)) {
						addLineServer(line)
					}
				}
			}
		}

		finalgui.addWindowAndWait(okno3)

	}

	private fun sendText(text : String){
		toServer.println(text)
	}

	private fun addLineServer(text : String){
		if(chatbox.tableModel.rowCount > 9){
			chatbox.tableModel.removeRow(0)
		}

		chatbox.tableModel.addRow(text)
	}

	private fun splitLine(text : String) : List<String>{
		return text.chunked(50)
	}

	private fun disconnect(){
		sendText("/disconnect")
		okno3.close()
		MessageDialog.showMessageDialog(finalgui, "Message", "Disconnected")
		System.exit(0)
	}
}

fun main(args: Array<String>) {
	OhWow()
}
