// const hw = 'hellow world by const'
// var fn = function(input){
//   return input.split(' ')
// }
// ReactDOM.render(
//   <h1>{fn(hw).map(x => x + '~')}</h1>,
//   document.getElementById('root')
// );

// function UKperson(props){
//   return <u>{props.name} is {props.state}ing</u>;
// }

//ES6 class style
class UKperson extends React.Component {
  render() {
    return (
      <u>
        {this.props.name} is {this.props.state}ing
      </u>
    );
  }
}

function Schedule(prop) {
  return (
    <div>
      <h2>Schedule board</h2>
      <ul>
        <li>
          <UKperson name="Tupi" state="study" />
        </li>

        <li>
          <UKperson name="Tupi" state="work" />
        </li>

        <li>
          <UKperson name="Sasha" state="sleep" />
        </li>

        <li>
          <UKperson name="Nick" state="work" />
        </li>
      </ul>
    </div>
  );
}

ReactDOM.render(<Schedule />, document.getElementById("cpn-root"));

class Clock extends React.Component {
  // 2. 1st step when React calls Clock component:
  //    initialize Clock DOM with props as necessary input
  constructor(props) {
    super(props);
    this.state = { date: new Date() }; // assign the current time to Clock's date property in state
  }
  // 4. 3rd step when React calls Clock component:
  //    after the Clock DOM displays on the screen,
  //    the lifecycle of Clock DOM is set up to call tick() in every second
  componentDidMount() {
    this.timerID = setInterval(() => this.tick(), 1000);
  }

  // 6. 5th step when React calls Clock component:
  //    if the Clock component is removed, the componentWillUnmount() lifecycle method tells React to stop the timer
  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  // 5. 4th step when React calls Clock component:
  //    the tick() calls setState and update the date property of state,
  //    !also, setState tells React that the state is changed and call render() again
  //    so React compares and knows the difference of this.state.date, then updates the DOM in screen
  tick() {
    this.setState({
      date: new Date(),
    });
  }

  // 3. 2nd step when React calls Clock component:
  //    render the content inside of Clock DOM
  render() {
    return (
      <div>
        <h1>Hello, world!</h1>
        <h2>It is {this.state.date.toLocaleTimeString()}.</h2>
      </div>
    );
  }
}

// 1. Render the Clock DOM and trigger the Clock component
ReactDOM.render(<Clock />, document.getElementById("root"));
